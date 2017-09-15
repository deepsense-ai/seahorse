# Copyright 2016, deepsense.ai
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
import time
from threading import Thread

import pika
from pika.exceptions import ConnectionClosed

from utils import Logging


class RabbitMQClient(Logging):
    _channel_impl = None

    def __init__(self, address, credentials, exchange, exchange_type='topic'):
        super(RabbitMQClient, self).__init__()
        self._address = address
        self._exchange = exchange
        self._credentials = credentials
        self._exchange_type = exchange_type

        self._reset_consumer_thread(start=False)

        self._declare_exchange()

    def send(self, topic, message):
        self._channel.basic_publish(exchange=self._exchange,
                                    routing_key=topic,
                                    body=message)

    def subscribe(self, topic, handler):
        queue_name = self._channel.queue_declare(exclusive=True).method.queue
        self._channel.queue_bind(exchange=self._exchange,
                                 queue=queue_name,
                                 routing_key=topic)

        self._channel.basic_consume(handler, queue=queue_name)

        if not self._consumer_thread.is_alive():
            self._reset_consumer_thread(start=True)

    def consume(self, inactivity_timeout, handler, timeout_handler):
        queue_name = self._channel.queue_declare(exclusive=True).method.queue
        self._channel.queue_bind(exchange=self._exchange,
                                 queue=queue_name)
        for message in self._channel.consume(queue=queue_name,
                                             inactivity_timeout=inactivity_timeout):
            if message is not None:
                handler(self._channel, message)
            else:
                timeout_handler()

    def _declare_exchange(self):
        self._channel.exchange_declare(exchange=self._exchange,
                                       exchange_type=self._exchange_type)

    def _reset_consumer_thread(self, start):
        self._consumer_thread = Thread(target=self._channel.start_consuming)
        self._consumer_thread.daemon = True
        if start:
            assert not self._consumer_thread.is_alive()
            self._consumer_thread.start()

    @property
    def _channel(self):
        if not self._channel_impl:
            connection = self._establish_connection_to_mq(self._address, self._credentials)
            self._channel_impl = connection.channel()
        return self._channel_impl

    @staticmethod
    def _establish_connection_to_mq(address, credentials):
        while True:
            try:
                return pika.BlockingConnection(
                    pika.ConnectionParameters(host=address[0], port=address[1],
                        credentials=pika.PlainCredentials(credentials[0], credentials[1])))
            except ConnectionClosed:
                time.sleep(1)


class RabbitMQJsonSender(Logging):
    def __init__(self, rabbit_mq_client, topic):
        super(RabbitMQJsonSender, self).__init__()
        self._rabbit_mq_client = rabbit_mq_client
        self._topic = topic

    def send(self, message):
        try:
            json_message = json.dumps(message)
        except Exception as e:
            self.logger.debug('JSON serialization failed: {}. Message: {}'.format(e, message))
            return

        self._rabbit_mq_client.send(topic=self._topic,
                                    message=json_message)


class RabbitMQJsonReceiver(Logging):
    def __init__(self, rabbit_mq_client):
        super(RabbitMQJsonReceiver, self).__init__()
        self._rabbit_mq_client = rabbit_mq_client

    def subscribe(self, topic, handler):
        self._rabbit_mq_client.subscribe(topic, self._wrapped_handler(handler))
        self.logger.debug('Subscribed to topic {}'.format(topic))

    @staticmethod
    def _wrapped_handler(actual_handler):
        # noinspection PyUnusedLocal
        def handle(ch, method, properties, body):
            message = json.loads(body)
            return actual_handler(message)
        return handle
