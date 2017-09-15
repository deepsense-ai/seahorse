# Copyright (c) 2016, CodiLime Inc.

import os
import base64
import zmq
import json

from jupyter_client.session import Session

from rabbit_mq_client import RabbitMQClient, RabbitMQJsonReceiver, RabbitMQJsonSender
from socket_forwarder import SocketForwarder
from utils import Logging


class ExecutingKernelClientSettings:
    def __init__(self, gateway_address, r_backend_address,
                 rabbit_mq_address, rabbit_mq_credentials,
                 session_id, workflow_id, node_id=None, port_number=None,
                 dataframe_storage_type='input'):

        self._gateway_address = gateway_address
        self._r_backend_address = r_backend_address
        self._rabbit_mq_address = rabbit_mq_address
        self._rabbit_mq_credentials = rabbit_mq_credentials
        self._session_id = session_id
        self._workflow_id = workflow_id
        self._node_id = node_id
        self._port_number = port_number
        self._dataframe_storage_type = dataframe_storage_type

    @property
    def dataframe_source(self):
        return self._workflow_id, self._node_id, self._port_number, self._dataframe_storage_type

    @property
    def gateway_address(self):
        return self._gateway_address

    @property
    def r_backend_address(self):
        return self._r_backend_address

    @property
    def rabbit_mq_address(self):
        return self._rabbit_mq_address

    @property
    def rabbit_mq_credentials(self):
        return self._rabbit_mq_credentials


class ExecutingKernelClient(Logging):
    EXCHANGE = 'remote_notebook_kernel'
    EXECUTION_PUBLISHING_TOPIC = 'execution.{kernel_id}.from_external'
    EXECUTION_SUBSCRIPTION_TOPIC = 'execution.{kernel_id}.to_external'

    def __init__(self, kernel_id, signature_key, executing_kernel_client_settings):
        super(ExecutingKernelClient, self).__init__()
        self.client_settings = executing_kernel_client_settings
        self.kernel_id = kernel_id
        self.context = zmq.Context()
        self.session = Session(key=signature_key)
        self.subscriber = {}

        self._rabbit_sender_client, self._rabbit_listener = self._init_rabbit_clients()
        self._socket_forwarders = self._init_socket_forwarders()

    def start(self):
        self._rabbit_listener.subscribe(
            topic=self.EXECUTION_SUBSCRIPTION_TOPIC.format(kernel_id=self.kernel_id),
            handler=self._handle_execution_message_from_rabbit)

        for forwarder in self._socket_forwarders.itervalues():
            forwarder.start()

        self._init_kernel()

    def _init_kernel(self):
        connection_dict = self.get_connection_file_dict()
        kernel_name = connection_dict['kernel_name']
        workflow_id, node_id, port_number, dataframe_storage_type = self.client_settings.dataframe_source
        gateway_host, gateway_port = self.client_settings.gateway_address
        r_backend_host, r_backend_port = self.client_settings.r_backend_address

        # The following work both in Python and R
        self._execute_code('workflow_id = "{}"'.format(workflow_id))
        self._execute_code('node_id = {}'.format(
            '"{}"'.format(node_id) if node_id is not None else None))
        self._execute_code('dataframe_storage_type = "{}"'.format(dataframe_storage_type))
        self._execute_code('port_number = {}'.format(port_number))
        self._execute_code('gateway_address = "{}"'.format(gateway_host))
        self._execute_code('gateway_port = {}'.format(gateway_port))
        self._execute_code('r_backend_host = "{}"'.format(r_backend_host))
        self._execute_code('r_backend_port = {}'.format(r_backend_port))

        if kernel_name == 'PythonExecutingKernel':
            self._execute_file(os.path.join(os.getcwd(), 'executing_kernels/python/kernel_init.py'))
        elif kernel_name == 'RExecutingKernel':
            self._execute_file(os.path.join(os.getcwd(), 'executing_kernels/r/kernel_init.R'))

    def _send_zmq_forward_to_rabbit(self, stream_name, message):
        if not isinstance(message, list):
            self._exit('ExecutingKernel::_send_zmq_forward_to_rabbit: Malformed message')

        self._rabbit_sender_client.send({
            'type': 'zmq_socket_forward',
            'stream': stream_name,
            'body': [base64.b64encode(s) for s in message]
        })

    def _handle_execution_message_from_rabbit(self, message):
        known_message_types = ['zmq_socket_forward']
        if not isinstance(message, dict) or 'type' not in message \
                or message['type'] not in known_message_types:
            self._exit('ExecutingKernel::_handle_execution_message_from_rabbit: Unknown message: {}'
                       .format(message))

        if message['type'] == 'zmq_socket_forward':
            if 'stream' not in message or 'body' not in message:
                self._exit(
                    'ExecutingKernel::_handle_execution_message_from_rabbit: Malformed message: {}'.format(message))

            self.logger.debug('Sending to {}'.format(message['stream']))
            body = [base64.b64decode(s) for s in message['body']]
            self._socket_forwarders[message['stream']].forward_to_zmq(body)

    def _execute_code(self, code):
        content = dict(code=code, silent=True, user_variables=[],
                       user_expressions={}, allow_stdin=False)
        msg = self.session.msg('execute_request', content)
        ser = self.session.serialize(msg)

        self._socket_forwarders['shell'].forward_to_zmq(ser)

    def _execute_file(self, filename):
        with open(filename, 'r') as f:
            self._execute_code(f.read())

    def _exit(self, msg):
        self.logger.debug(msg)

    def _init_rabbit_clients(self):
        rabbit_client = RabbitMQClient(address=self.client_settings.rabbit_mq_address,
                                       credentials=self.client_settings.rabbit_mq_credentials,
                                       exchange=self.EXCHANGE)
        sender = RabbitMQJsonSender(
            rabbit_mq_client=rabbit_client,
            topic=self.EXECUTION_PUBLISHING_TOPIC.format(kernel_id=self.kernel_id))
        listener = RabbitMQJsonReceiver(rabbit_client)
        return sender, listener

    def get_connection_file_dict(self):
        self.logger.debug('Reading connection file {}'.format(os.getcwd()))
        try:
            with open('kernel-' + self.kernel_id + '.json', 'r') as json_file:
                return json.load(json_file)
        except IOError as e:
            self.logger.error(os.strerror(e.errno))
            raise

    def _init_socket_forwarders(self):
        forwarders = {}
        kernel_json = self.get_connection_file_dict()

        def make_sender(stream_name):
            def sender(message):
                self._send_zmq_forward_to_rabbit(stream_name, message)
            return sender

        for socket in ['shell', 'control', 'stdin']:
            self.subscriber[socket] = self.context.socket(zmq.DEALER)

        # iopub is PUB socket, we treat it differently and have to set SUBSCRIPTION topic
        self.subscriber['iopub'] = self.context.socket(zmq.SUB)
        self.subscriber['iopub'].setsockopt(zmq.SUBSCRIBE, b'')

        for (socket, zmq_socket) in self.subscriber.iteritems():
            zmq_socket.connect('tcp://localhost:' + str(kernel_json[socket + '_port']))
            forwarders[socket] = SocketForwarder(
                stream_name=socket,
                zmq_socket=zmq_socket,
                to_rabbit_sender=make_sender(socket))

        return forwarders
