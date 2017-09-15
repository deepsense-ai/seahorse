# Copyright (c) 2015, CodiLime Inc.

#!/usr/bin/env python
import json
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='172.28.128.1'))
channel = connection.channel()

channel.queue_declare(queue='gateway_requests')
channel.queue_declare(queue='gateway_responses')

def respond_with_gateway_location(ch, method, properties, body):
    request = json.loads(body)
    print request
    message_body = {
        "messageType": "gatewayAddress",
        "messageBody": {
            "hostname": "localhost",
            "port": 25555
        }
    }
    channel.basic_publish(exchange='', routing_key='gateway_responses', body=json.dumps(message_body))

channel.basic_consume(respond_with_gateway_location,
                      queue='gateway_requests',
                      no_ack=True)

channel.start_consuming()
