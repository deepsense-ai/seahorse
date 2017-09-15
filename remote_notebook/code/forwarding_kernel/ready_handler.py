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

from rabbit_mq_client import RabbitMQClient
from utils import Logging


class ReadyHandler(Logging):

    def __init__(self, rabbit_mq_address, rabbit_mq_credentials, session_id):
        super(ReadyHandler, self).__init__()
        self._connection = RabbitMQClient(address=rabbit_mq_address,
                                          credentials=rabbit_mq_credentials,
                                          exchange='seahorse_ready_{}'.format(session_id),
                                          exchange_type='fanout')

    def handle_ready(self, on_ready):
        # noinspection PyUnusedLocal
        def handle_message(ch, method, properties, body):
            response = json.loads(body)
            if response['messageType'] == 'ready':
                self.logger.debug("Received 'ready'. Calling on_ready callback")
                ch.basic_ack(method.delivery_tag)
                on_ready()

        thread = Thread(target=lambda: self._connection.subscribe(topic=None,
                                                                  handler=handle_message))
        thread.daemon = True
        thread.start()
