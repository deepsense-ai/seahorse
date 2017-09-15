# Copyright (c) 2016, CodiLime Inc.

from utils import started_daemon_thread, debug


class SocketForwarder(object):
    """
    This class is responsible for forwarding traffic between
    RabbitMQ and a ZMQ socket in both directions.

    Forwarding from ZMQ to Rabbit is done in a thread,
    while sending to Rabbit is achieved by calling forward_to_zmq method.
    """

    def __init__(self, stream_name, zmq_socket, to_rabbit_sender):
        self.stream_name = stream_name
        self.zmq_socket = zmq_socket
        self.to_rabbit_sender = to_rabbit_sender
        self.to_rabbit_forwarding_thread = None

        self._received_message_from_rabbit = False

    @property
    def received_message_from_rabbit(self):
        """
        This flag informs if the "other end" of the "connection"
        has sent something already.
        """
        return self._received_message_from_rabbit

    def start(self):
        debug('Started {} SocketForwarder.'.format(self.stream_name))
        self.to_rabbit_forwarding_thread = started_daemon_thread(target=self.to_rabbit_forwarder)

    def forward_to_zmq(self, message):
        debug('ZMQForwarder[{}]::forward_to_zmq: Sending {}'.format(self.stream_name, message))
        self.zmq_socket.send_multipart(message)
        self._received_message_from_rabbit = True

    def to_rabbit_forwarder(self):
        while True:
            message = self.zmq_socket.recv_multipart()
            debug('ZMQForwarder[{}]::to_rabbit_forwarder: Sending {}'.format(self.stream_name, message))
            self.to_rabbit_sender(message)


class ToZmqSocketForwarder(SocketForwarder):
    """
    This subclass of SocketForwarded doesn't forward messages
    from ZMQ to Rabbit.

    This is useful for sockets that don't support receiving, like PUBs.
    """
    def start(self):
        debug('ToZmqSocketForwarder[{}]::start: NOT starting forwarding to Rabbit'.format(self.stream_name))
