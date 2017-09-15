# Copyright (c) 2016, CodiLime Inc.

import base64
import sys
import os

import zmq
from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type

from rabbit_mq_client import RabbitMQClient, RabbitMQJsonSender, RabbitMQJsonReceiver
from socket_forwarder import SocketForwarder
from utils import setup_logging, Logging


def extract_argument(argv, arg_name):
    return argv[argv.index(arg_name) + 1]


kernel_id = extract_argument(sys.argv, '--kernel-id')


class ExecutingKernel(IPythonKernel, Logging):
    """
    This is where the actual code execution happens.

    This kernel listens on messages sent via RabbitMQ and forwards them
    to appropriate ZMQ sockets of the actual IPython kernel. Similarly,
    all traffic sent via ZMQ sockets ends up in RabbitMQ.
    """

    EXCHANGE = 'remote_notebook_kernel'
    EXECUTION_PUBLISHING_TOPIC = 'execution.{kernel_id}.from_external'
    EXECUTION_SUBSCRIPTION_TOPIC = 'execution.{kernel_id}.to_external'

    ZMQ_IDENTITY = 'ExecutingKernel'

    def __init__(self, **kwargs):
        super(ExecutingKernel, self).__init__(**kwargs)

        self._rabbit_sender_client, self._rabbit_listener = self._init_rabbit_clients()
        self._socket_forwarders = self._init_socket_forwarders()

    def start(self):
        super(ExecutingKernel, self).start()
        self._rabbit_listener.subscribe(topic=self.EXECUTION_SUBSCRIPTION_TOPIC.format(kernel_id=kernel_id),
                                        handler=self._handle_execution_message_from_rabbit)

        self.session.key = self._signature_key

        for forwarder in self._socket_forwarders.itervalues():
            forwarder.start()

        self._init_kernel()

    def _init_kernel(self):
        gateway_host, gateway_port = self._gateway_address
        kernel_init_file = os.path.join(os.getcwd(), "executing_kernel/kernel_init.py")

        workflow_id, node_id, port_number = self._dataframe_source

        self._execute_code('kernel_id = "{}"'.format(kernel_id))
        self._execute_code('workflow_id = "{}"'.format(workflow_id))
        if node_id:
            self._execute_code('node_id = "{}"'.format(node_id))
        else:
            self._execute_code('node_id = None')
        self._execute_code('port_number = {}'.format(port_number))

        self._execute_code('gateway_address = "{}"'.format(gateway_host))
        self._execute_code('gateway_port = {}'.format(gateway_port))
        self._execute_code(open(kernel_init_file, "r").read())

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
        if not isinstance(message, dict) or 'type' not in message or message['type'] not in known_message_types:
            self._exit('ExecutingKernel::_handle_execution_message_from_rabbit: Unknown message: {}'.format(message))

        if message['type'] == 'zmq_socket_forward':
            if 'stream' not in message or 'body' not in message:
                self._exit('ExecutingKernel::_handle_execution_message_from_rabbit: Malformed message: {}'
                           .format(message))

                self.logger.debug('Sending to {}'.format(message['stream']))
            body = [base64.b64decode(s) for s in message['body']]
            self._socket_forwarders[message['stream']].forward_to_zmq(body)

    def _execute_code(self, code):
        content = dict(code=code, silent=True, user_variables=[], user_expressions={}, allow_stdin=False)
        msg = self.session.msg('execute_request', content)
        ser = self.session.serialize(msg)
        self._socket_forwarders['shell'].forward_to_zmq(ser)

    def _exit(self, msg):
        self.logger.debug(msg)
        sys.exit(1)

    @property
    def _dataframe_source(self):
        def value_or_none(f):
            try:
                return f()
            except ValueError:
                return None

        workflow_id = extract_argument(self.parent.argv, '--workflow-id')
        node_id = value_or_none(lambda: extract_argument(self.parent.argv, '--node-id'))
        port_number = value_or_none(lambda: int(extract_argument(self.parent.argv, '--port-number')))

        return workflow_id, node_id, port_number

    @property
    def _signature_key(self):
        return extract_argument(self.parent.argv, '--signature-key')

    @property
    def _gateway_address(self):
        host = extract_argument(self.parent.argv, '--gateway-host')
        port = extract_argument(self.parent.argv, '--gateway-port')
        return host, int(port)

    @property
    def _rabbit_mq_address(self):
        host = extract_argument(self.parent.argv, '--mq-host')
        port = extract_argument(self.parent.argv, '--mq-port')
        return host, int(port)

    @property
    def _rabbit_mq_credentials(self):
        user = self._extract_argument(self.parent.argv, '--mq-user')
        password = self._extract_argument(self.parent.argv, '--mq-pass')
        return user, password

    def _init_rabbit_clients(self):
        rabbit_client = RabbitMQClient(address=self._rabbit_mq_address,
                                       credentials=self._rabbit_mq_credentials,
                                       exchange=self.EXCHANGE)
        sender = RabbitMQJsonSender(rabbit_mq_client=rabbit_client,
                                    topic=self.EXECUTION_PUBLISHING_TOPIC.format(kernel_id=kernel_id))
        listener = RabbitMQJsonReceiver(rabbit_client)
        return sender, listener

    def _init_socket_forwarders(self):
        forwarders = {}

        def make_sender(stream_name):
            def sender(message):
                self._send_zmq_forward_to_rabbit(stream_name, message)
            return sender

        for socket in ['shell', 'control', 'stdin', 'iopub']:
            get_connected_socket = getattr(self.parent, 'connect_{}'.format(socket))
            forwarders[socket] = SocketForwarder(
                stream_name=socket,
                zmq_socket=get_connected_socket(identity=self.ZMQ_IDENTITY),
                to_rabbit_sender=make_sender(socket))

        return forwarders


class ExecutingKernelApp(IPKernelApp):
    kernel_class = Type(ExecutingKernel, config=True)
    log_level = 'DEBUG'


if __name__ == '__main__':
    setup_logging(os.path.join(os.getcwd(), 'executing_kernel_logs', kernel_id + '.log'))

    app = ExecutingKernelApp.instance(context=zmq.Context.instance())
    app.initialize()
    app.start()
