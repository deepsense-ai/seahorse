# Copyright (c) 2015, CodiLime Inc.

import json
from threading import Thread

import pika
import time

from pika.exceptions import ConnectionClosed


class ReadyHandler(object):

    SEAHORSE_MQ_EXCHANGE = 'seahorse'
    TO_NOTEBOOK_TOPIC = 'notebook.to'

    def __init__(self, rabbit_mq_address):
        self.rabbit_mq_address = rabbit_mq_address

    def handle_ready(self, on_ready):
        Thread(target=lambda: self._handler_thread(on_ready)).start()

    def _handler_thread(self, on_ready):
        connection = self._establish_connection_to_mq(self.rabbit_mq_address)

        channel = connection.channel()
        channel.exchange_declare(exchange=self.SEAHORSE_MQ_EXCHANGE,
                                 exchange_type='topic')
        queue_name = channel.queue_declare(exclusive=True).method.queue

        channel.queue_bind(exchange=self.SEAHORSE_MQ_EXCHANGE,
                           queue=queue_name,
                           routing_key=self.TO_NOTEBOOK_TOPIC)

        # noinspection PyUnusedLocal
        def handle_message(ch, method, properties, body):
            response = json.loads(body)
            if response['messageType'] == 'ready':
                on_ready()

        channel.basic_consume(handle_message, queue=queue_name)
        channel.start_consuming()
        connection.close()

    @staticmethod
    def _establish_connection_to_mq(address):
        while True:
            try:
                return pika.BlockingConnection(
                    pika.ConnectionParameters(host=address[0],
                                              port=address[1]))
            except ConnectionClosed:
                time.sleep(1)
