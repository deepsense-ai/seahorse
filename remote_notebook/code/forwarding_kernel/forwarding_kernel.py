# Copyright (c) 2016, CodiLime Inc.

import base64
import json

import os
import sys

import re
import argparse
import time
from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type

from rabbit_mq_client import RabbitMQClient, RabbitMQJsonSender, RabbitMQJsonReceiver
from ready_handler import ReadyHandler
from heartbeat_handler import HeartbeatHandler
from socket_forwarder import SocketForwarder, ToZmqSocketForwarder
from notebook_server_client import NotebookServerClient
from utils import setup_logging, Logging

import hashlib



RABBIT_MQ_ADDRESS = None
RABBIT_MQ_CREDENTIALS = None
HEARTBEAT_INTERVAL = None
MISSED_HEARTBEAT_LIMIT = None
KERNEL_NAME = None


class ForwardingKernel(IPythonKernel, Logging):
    """
    This kernel is what the Notebook Server runs.

    No actual IPython kernel is run here, but all ZMQ sockets are opened
    and all traffic coming their way is captured and sent to the Executing Kernel
    via RabbitMQ. Conversely, all messages from RabbitMQ are forwarded
    to appropriate ZMQ sockets.

    To the outside observer (Notebook Server), the fact that the code is executed
    remotely, is invisible.
    """
    _kernel_id_impl = None
    _signature_key_impl = None

    EXCHANGE = 'remote_notebook_kernel'
    MANAGEMENT_PUBLICATION_TOPIC = 'management.{session_id}.{kernel_id}.to_manager'
    MANAGEMENT_SUBSCRIPTION_TOPIC = 'management.{session_id}.{kernel_id}.from_manager'
    EXECUTION_PUBLISHING_TOPIC = 'execution.{kernel_id}.to_external'
    EXECUTION_SUBSCRIPTION_TOPIC = 'execution.{kernel_id}.from_external'

    # noinspection PyMissingConstructor
    def __init__(self, parent):
        Logging.__init__(self)

        self.parent = parent
        self._init_argv()
        nb_client = NotebookServerClient("localhost", 8888, self._kernel_id,
                                         seahorse_notebook_path=self.argv.seahorse_notebook_path)
        self._session_id, self._node_id, self._port_number = nb_client.extract_dataframe_source()

        es, ms, rl = self._init_rabbit_clients(RABBIT_MQ_ADDRESS, RABBIT_MQ_CREDENTIALS)
        self._rabbit_execution_sender_client = es
        self._rabbit_management_sender_client = ms
        self._rabbit_listener = rl

        self._socket_forwarders = self._init_socket_forwarders()

        mq_address, mq_port = RABBIT_MQ_ADDRESS
        self._ready_handler = ReadyHandler([mq_address, mq_port], RABBIT_MQ_CREDENTIALS, self._session_id)
        self._ready_handler.handle_ready(nb_client.restart_kernel)

        self._heartbeat_handler = HeartbeatHandler([mq_address, mq_port],
                                                   RABBIT_MQ_CREDENTIALS,
                                                   HEARTBEAT_INTERVAL,
                                                   MISSED_HEARTBEAT_LIMIT,
                                                   self._session_id)
        self._heartbeat_handler.handle_heartbeat(lambda: nb_client.stop_kernel())

    def _init_argv(self):
        parser = argparse.ArgumentParser()
        parser.add_argument("--seahorse_notebook_path", help="string with searhorse path", required=False)
        (self.argv, sys.argv) = parser.parse_known_args()
        # TODO fail if seahorse_notebook_path is not in right format

    def _init_rabbit_clients(self, rabbit_mq_address, rabbit_mq_credentials):
        rabbit_client = RabbitMQClient(address=rabbit_mq_address,
                                       credentials=rabbit_mq_credentials,
                                       exchange=self.EXCHANGE)
        execution_sender = RabbitMQJsonSender(
            rabbit_mq_client=rabbit_client,
            topic=self.EXECUTION_PUBLISHING_TOPIC.format(kernel_id=self._kernel_id))
        management_sender = RabbitMQJsonSender(
            rabbit_mq_client=rabbit_client,
            topic=self.MANAGEMENT_PUBLICATION_TOPIC.format(session_id=self._session_id, kernel_id=self._kernel_id))
        listener = RabbitMQJsonReceiver(rabbit_client)
        return execution_sender, management_sender, listener

    def _send_zmq_forward_to_rabbit(self, stream_name, message):
        if not isinstance(message, list):
            self._exit('Malformed message')

        self._rabbit_execution_sender_client.send({
            'type': 'zmq_socket_forward',
            'stream': stream_name,
            'body': [base64.b64encode(s) for s in message]
        })

    def _handle_execution_message_from_rabbit(self, message):
        known_message_types = ['zmq_socket_forward']
        if not isinstance(message, dict) or 'type' not in message or message['type'] not in known_message_types:
            self._exit('Unknown message: {}'.format(message))

        if message['type'] == 'zmq_socket_forward':
            if 'stream' not in message or 'body' not in message:
                self._exit('Malformed message: {}'.format(message))

            body = [base64.b64decode(s) for s in message['body']]
            self._socket_forwarders[message['stream']].forward_to_zmq(body)

    def _init_socket_forwarders(self):
        forwarders = {}

        def make_sender(stream_name):
            def sender(message):
                self._send_zmq_forward_to_rabbit(stream_name, message)

            return sender

        for socket in ['shell', 'control', 'stdin', 'iopub']:
            zmq_socket = getattr(self.parent, '{}_socket'.format(socket))
            factory = SocketForwarder if socket != 'iopub' else ToZmqSocketForwarder
            forwarders[socket] = factory(
                stream_name=socket,
                zmq_socket=zmq_socket,
                to_rabbit_sender=make_sender(socket))
        return forwarders

    def start(self):
        # If seahorse_notebook_path is not defined then we are in an interactive mode, where user
        # executes cells from the UI and should use output frame of notebook parent.
        # If seahorse_notebook_path is defined then we are in headless mode. Notebook is executed
        # by Session Executor. We should use input frame of notebook node, because in this case, the
        # operation is able to register the incoming dataframe as its input in dataFrameStorage.
        dataframe_storage_type = 'output' if self.argv.seahorse_notebook_path is None else 'input'
        self._rabbit_management_sender_client.send({
            'type': 'start_kernel',
            'kernel_id': self._kernel_id,
            'kernel_name': KERNEL_NAME,
            'signature_key': self._signature_key,
            'dataframe_source': {
                'dataframe_storage_type': dataframe_storage_type,
                'node_id': self._node_id,
                'port_number': self._port_number
                },
        })

        self._rabbit_listener.subscribe(topic=self.EXECUTION_SUBSCRIPTION_TOPIC.format(kernel_id=self._kernel_id),
                                        handler=self._handle_execution_message_from_rabbit)

        while not any([f.received_message_from_rabbit for f in self._socket_forwarders.itervalues()]):
            self.logger.debug('Still waiting for executing kernel')
            time.sleep(.1)

        for forwarder in self._socket_forwarders.itervalues():
            forwarder.start()

        self.logger.debug('Started!')

    def _exit(self, msg):
        self.logger.debug(msg)
        sys.exit(1)

    @property
    def _kernel_id(self):
        if not self._kernel_id_impl:
            self._kernel_id_impl = self.get_kernel_id(self.parent.connection_file)
        return self._kernel_id_impl

    @staticmethod
    def get_kernel_id(connection_file):
        m = re.search('kernel-(?P<kernel_id>.*)\.json', connection_file)
        if m is not None:
            return m.group('kernel_id')
        else:
            m = hashlib.md5()
            m.update(connection_file)
            return str(m.hexdigest())

    @property
    def _signature_key(self):
        if not self._signature_key_impl:
            connection_dict = json.loads(open(self.parent.connection_file, 'r').read())
            self._signature_key_impl = connection_dict['key']
        return self._signature_key_impl


class ForwardingKernelApp(IPKernelApp):
    kernel_class = Type(ForwardingKernel, config=True)
    log_level = 'DEBUG'

    def initialize(self, argv=None):
        super(IPKernelApp, self).initialize(argv)  # Skipping IPKernelApp.initialize on purpose

        self.init_connection_file()
        self.init_poller()
        self.init_sockets()
        self.init_heartbeat()
        self.init_signal()

    def initialize_kernel(self):
        self.kernel = ForwardingKernel(parent=self)


if __name__ == '__main__':
    RABBIT_MQ_ADDRESS = (os.environ['MQ_HOST'], int(os.environ['MQ_PORT']))
    RABBIT_MQ_CREDENTIALS = (os.environ['MQ_USER'], os.environ['MQ_PASS'])
    MISSED_HEARTBEAT_LIMIT = int(os.environ['MISSED_HEARTBEAT_LIMIT'])
    HEARTBEAT_INTERVAL = float(os.environ['HEARTBEAT_INTERVAL'])
    KERNEL_NAME = os.environ['KERNEL_NAME']

    app = ForwardingKernelApp.instance()
    app.initialize()

    setup_logging(os.path.join(os.getcwd(),
                               'forwarding_kernel_logs',
                               'kernel_{}.log'.format(ForwardingKernel.get_kernel_id(app.connection_file))))

    app.initialize_kernel()
    app.start()
