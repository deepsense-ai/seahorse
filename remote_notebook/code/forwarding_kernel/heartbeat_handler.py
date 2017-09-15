# Copyright (c) 2016, CodiLime Inc.

import json
from threading import Thread

import pika
import time

from pika.exceptions import ConnectionClosed
from utils import debug
from rabbit_mq_client import RabbitMQClient


class HeartbeatHandler(object):

    def __init__(self, rabbit_mq_address, rabbit_mq_credentials, heartbeat_interval, missed_heartbeat_limit, session_id):
        self._rabbit_mq_address = rabbit_mq_address
        self._rabbit_mq_credentials = rabbit_mq_credentials
        self._heartbeat_interval = heartbeat_interval
        self._missed_heartbeat_limit = missed_heartbeat_limit
        self._exchange_name = 'seahorse_heartbeats_{}'.format(session_id)
        self._session_id = session_id
        self._missed_heartbeats = 0

    def handle_heartbeat(self, on_heartbeat_error):
        """
        :param on_heartbeat_error: Called when multiple heartbeats are missed.
        """
        debug("Heartbeat::handle_heartbeat starting thread")
        thread = Thread(target=lambda: self._handler_thread(on_heartbeat_error))
        thread.daemon = True
        thread.start()

    def _handler_thread(self, on_heartbeat_error):
        debug("HeartbeatHandler::_handler_thread")
        connection = RabbitMQClient(address=self._rabbit_mq_address,
                                    credentials=self._rabbit_mq_credentials,
                                    exchange=self._exchange_name,
                                    exchange_type='fanout')

        def heartbeat_missed():
            debug("HeartbeatHandler::heartbeat_missed ({}) {}".format(self._session_id,
                                                                      self._missed_heartbeats))
            self._missed_heartbeats += 1
            limit_exceeded = self._missed_heartbeats >= self._missed_heartbeat_limit
            if limit_exceeded:
                on_heartbeat_error()

        def handle_message(channel, message):
            method, properties, body = message
            response = json.loads(body)
            if response['messageType'] == 'heartbeat':
                channel.basic_ack(method.delivery_tag)
                self._missed_heartbeats = 0
            else:
                assert("Received unexpected message: {}".format(response))

        connection.consume(self._heartbeat_interval, handle_message, heartbeat_missed)
