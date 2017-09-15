# Copyright (c) 2015, CodiLime Inc.

import json
from threading import Thread

import pika
import time

from pika.exceptions import ConnectionClosed


class GatewayResolver(object):
    """
    The purpose of this class is to retrieve
    PythonGateway server address from Session Executor via RabbitMQ
    """

    SEAHORSE_MQ_EXCHANGE = 'seahorse'
    TO_KERNEL_TOPIC = 'to_kernel'
    TO_EXECUTOR_TOPIC = 'to_executor'

    # This is used when no actual workflow_id is available
    FAKE_WORKFLOW_ID = 'deadbeef-0000-0000-0000-000000000000'

    def __init__(self, rabbit_mq_address, workflow_id=None):
        self.rabbit_mq_address = rabbit_mq_address
        self.workflow_id = workflow_id or self.FAKE_WORKFLOW_ID

    class Address(object):
        def __init__(self):
            self._host, self._port = None, None

        def received(self):
            return self._host is not None

        def set(self, host, port):
            self._host, self._port = host, int(port)

        def as_tuple(self):
            return self._host, self._port

    def get_gateway_address(self):
        connection = self._establish_connection_to_mq(self.rabbit_mq_address)

        channel = connection.channel()
        channel.exchange_declare(exchange=self.SEAHORSE_MQ_EXCHANGE,
                                 exchange_type='direct')
        queue_name = channel.queue_declare(exclusive=True).method.queue

        channel.queue_bind(exchange=self.SEAHORSE_MQ_EXCHANGE,
                           queue=queue_name,
                           routing_key=self.TO_KERNEL_TOPIC)

        address = self.Address()

        def keep_sending():
            return not address.received()

        request_sender_thread = Thread(target=lambda: self._request_sender(channel, keep_sending))
        request_sender_thread.daemon = True
        request_sender_thread.start()

        # noinspection PyUnusedLocal
        def handle_message(ch, method, properties, body):
            response = json.loads(body)
            if response['messageType'] == 'pythonGatewayAddress':
                first_address = response['messageBody']['addresses'][0]
                address.set(first_address['hostname'], first_address['port'])

                channel.stop_consuming()

        channel.basic_consume(handle_message, queue=queue_name)
        channel.start_consuming()
        connection.close()

        return address.as_tuple()

    def _request_sender(self, channel, keep_sending):
        get_python_gateway_address = {
            'messageType': 'getPythonGatewayAddress',
            "messageBody": {
                'workflowId': self.workflow_id
            }
        }

        while keep_sending():
            channel.basic_publish(exchange=self.SEAHORSE_MQ_EXCHANGE,
                                  routing_key=self.TO_EXECUTOR_TOPIC,
                                  body=json.dumps(get_python_gateway_address))
            time.sleep(1)

    @staticmethod
    def _establish_connection_to_mq(address):
        while True:
            try:
                return pika.BlockingConnection(
                    pika.ConnectionParameters(host=address[0],
                                              port=address[1]))
            except ConnectionClosed:
                time.sleep(1)
