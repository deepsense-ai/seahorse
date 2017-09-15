# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import json
from threading import Thread

import pika
import time

from pika.exceptions import ConnectionClosed
from utils import Logging
from rabbit_mq_client import RabbitMQClient


class HeartbeatHandler(Logging):

    def __init__(self, rabbit_mq_address, rabbit_mq_credentials, heartbeat_interval, missed_heartbeat_limit, session_id):
        super(HeartbeatHandler, self).__init__()
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
        self.logger.debug("Starting thread")
        thread = Thread(target=lambda: self._handler_thread(on_heartbeat_error))
        thread.daemon = True
        thread.start()

    def _handler_thread(self, on_heartbeat_error):
        self.logger.debug("Starting")
        connection = RabbitMQClient(address=self._rabbit_mq_address,
                                    credentials=self._rabbit_mq_credentials,
                                    exchange=self._exchange_name,
                                    exchange_type='fanout')

        def heartbeat_missed():
            self.logger.debug("({}) {}".format(self._session_id, self._missed_heartbeats))
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
