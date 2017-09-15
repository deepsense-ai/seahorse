# Copyright (c) 2016, CodiLime Inc.

import base64
import json

import os
import sys

import re

import time
from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type

from rabbit_mq_client import RabbitMQClient, RabbitMQJsonSender, RabbitMQJsonReceiver
from ready_handler import ReadyHandler
from socket_forwarder import SocketForwarder, ToZmqSocketForwarder
from notebook_server_client import NotebookServerClient
from utils import debug


RABBIT_MQ_ADDRESS = None


class ForwardingKernel(IPythonKernel):
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
        self.parent = parent

        nb_client = NotebookServerClient("localhost", 8888, self._kernel_id)
        self._session_id, self._node_id, self._port_number = nb_client.extract_dataframe_source()

        es, ms, rl = self._init_rabbit_clients(RABBIT_MQ_ADDRESS)
        self._rabbit_execution_sender_client = es
        self._rabbit_management_sender_client = ms
        self._rabbit_listener = rl

        self._socket_forwarders = self._init_socket_forwarders()

        mq_address, mq_port = RABBIT_MQ_ADDRESS
        self._ready_handler = ReadyHandler([mq_address, mq_port])
        self._ready_handler.handle_ready(nb_client.restart_kernel)

    def _init_rabbit_clients(self, rabbit_mq_address):
        rabbit_client = RabbitMQClient(address=rabbit_mq_address, exchange=self.EXCHANGE)
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
        self._rabbit_management_sender_client.send({
            'type': 'start_kernel',
            'kernel_id': self._kernel_id,
            'signature_key': self._signature_key,
            'node_id': self._node_id,
            'port_number': self._port_number
        })

        self._rabbit_listener.subscribe(topic=self.EXECUTION_SUBSCRIPTION_TOPIC.format(kernel_id=self._kernel_id),
                                        handler=self._handle_execution_message_from_rabbit)

        while not any([f.received_message_from_rabbit for f in self._socket_forwarders.itervalues()]):
            time.sleep(.1)

        for forwarder in self._socket_forwarders.itervalues():
            forwarder.start()

        debug('ForwardingKernel::start: Started!')

    @staticmethod
    def _exit(msg):
        debug(msg)
        sys.exit(1)

    @property
    def _kernel_id(self):
        if not self._kernel_id_impl:
            m = re.search('kernel-(?P<kernel_id>.*)\.json', self.parent.connection_file)
            self._kernel_id_impl = m.group('kernel_id')
        return self._kernel_id_impl

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
        self.kernel = ForwardingKernel(parent=self)

if __name__ == '__main__':
    RABBIT_MQ_ADDRESS = (os.environ['MQ_HOST'], int(os.environ['MQ_PORT']))

    app = ForwardingKernelApp.instance()
    app.initialize()
    app.start()
