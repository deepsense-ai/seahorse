# Copyright (c) 2016, CodiLime Inc.

import json
from threading import Thread

from rabbit_mq_client import RabbitMQClient
from utils import debug


class ReadyHandler(object):

    def __init__(self, rabbit_mq_address, session_id):
        self._connection = RabbitMQClient(address=rabbit_mq_address,
                                          exchange='seahorse_ready_{}'.format(session_id),
                                          exchange_type='fanout')

    def handle_ready(self, on_ready):
        # noinspection PyUnusedLocal
        def handle_message(ch, method, properties, body):
            response = json.loads(body)
            if response['messageType'] == 'ready':
                debug("ReadyHandler::handle_ready: Received 'ready'. Calling on_ready callback")
                ch.basic_ack(method.delivery_tag)
                on_ready()

        thread = Thread(target=lambda: self._connection.subscribe(topic=None,
                                                                  handler=handle_message))
        thread.daemon = True
        thread.start()
