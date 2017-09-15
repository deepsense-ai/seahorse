# Copyright (c) 2015, CodiLime Inc.

import pika
from pika.credentials import PlainCredentials


class Ex:
  exchange = "seahorse"
  type = "topic"


class To:
  kernel = "to_kernel"
  editor = "to_editor"
  executor = "to_executor"
  all = [kernel, editor, executor]


class Config:
  def __init__(self, host="localhost", port=5672, username="guest", password="guest"):
    self.host = host
    self.port = port
    self.username = username
    self.password = password


class SeahorseAMQPConnector(object):
  def __init__(self, config):
    super(SeahorseAMQPConnector, self).__init__()
    self.connection = pika.BlockingConnection(
      pika.ConnectionParameters(
        host=config.host,
        port=config.port,
        credentials=PlainCredentials(
          username=config.username,
          password=config.password)))
    self.channel = self.connection.channel()
    queue = self.channel.queue_declare(exclusive=True)
    self.queue_name = queue.method.queue
    self.channel.exchange_declare(exchange=Ex.exchange, type=Ex.type)

  def listen_to(self, routing_key):
    self.channel.queue_bind(
      exchange=Ex.exchange,
      queue=self.queue_name,
      routing_key=routing_key)

  def get_message(self):
    (method_frame, header_frame, body) = self.channel.basic_get(queue=self.queue_name)
    if method_frame is not None:
      return method_frame.routing_key, body
    else:
      return None, None

  def publish_message(self, routing_key, message_body):
    self.channel.basic_publish(
      exchange=Ex.exchange,
      routing_key=routing_key,
      body=message_body)
