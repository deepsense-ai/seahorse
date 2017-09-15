# Copyright (c) 2015, CodiLime Inc.

import commons
import time
import json
import json_pattern_utils
from json_pattern_utils import load_json
from SeahorseAMQPConnector import To, Config, SeahorseAMQPConnector


class InteractiveTestsClient(object):
  def __init__(self):
    super(InteractiveTestsClient, self).__init__()
    conf = commons.confGetter('rabbitmq')
    config = Config(
      host=conf('host'),
      port=int(conf('port')),
      username=conf('username'),
      password=conf('password'))
    self.seahorse_connector = SeahorseAMQPConnector(config)
    self.expected_messages = []
    self.unexpected_messages = []
    for routing_key in To.all:
      self.seahorse_connector.listen_to(routing_key)

  def expect_message(self, routing_key, message_body):
    expected = (unicode(routing_key), json.loads(message_body))
    for unexpected in self.unexpected_messages:
      if self._check_message(unexpected, expected) or self._check_message(expected, unexpected):
        raise AssertionError("Expected or unexpected message is contained in another opposite: " +
                             "Unexpected: " + str(unexpected) + " Expected: " + str(expected))
    if routing_key not in To.all:
      raise AssertionError("Invalid routing key: " + routing_key + ". Choose from: " + str(To.all))
    self.expected_messages.append(expected)

  def expect_message_from_file(self, routing_key, message_path):
    content = self._read_content(message_path)
    self.expect_message(routing_key, content)

  def unexpected_message(self, routing_key, message_body):
    unexpected = (unicode(routing_key), json.loads(message_body))
    for expected in self.expected_messages:
      if self._check_message(unexpected, expected) or self._check_message(expected, unexpected):
        raise AssertionError("Expected or unexpected message shouldn't be contained in another: " +
                             "Unexpected: " + str(unexpected) + " Expected: " + str(expected))
    if routing_key not in To.all:
      raise AssertionError("Invalid routing key: " + routing_key + ". Choose from: " + str(To.all))
    self.unexpected_messages.append(unexpected)

  def unexpected_message_from_file(self, routing_key, message_path):
    content = self._read_content(message_path)
    self.unexpected_message(routing_key, content)

  def publish_message(self, routing_key, message_body):
    if routing_key not in To.all:
      raise AssertionError("Invalid routing key: " + routing_key + ". Choose from: " + str(To.all))
    self.seahorse_connector.publish_message(routing_key, message_body)

  def publish_message_from_file(self, routing_key, message_path):
    content = self._read_content(message_path)
    self.publish_message(routing_key, content)

  # TODO fix active loop to triggered by second thread or coroutine
  def validate_received_messages(self, str_timeout="5"):
    timeout = float(str_timeout)
    timestamp = time.time()
    while timestamp + timeout > time.time():
      next_expected = []
      message = self.seahorse_connector.get_message()
      if message != (None, None):
        routing_key, message_body = message
        message_parsed = (unicode(routing_key), json.loads(message_body))
        for expected in self.expected_messages:
          if not self._check_message(expected, message_parsed):
            next_expected.append(expected)
        self.expected_messages = next_expected
        for unexpected in self.unexpected_messages:
          if self._check_message(unexpected, message_parsed):
            raise AssertionError("Unexpected message was delivered: " +
                                 str(unexpected) + " in " + str(message_parsed))
        self.expected_messages = next_expected
        if self._are_expected_delivered():
          return None
      else:
        time.sleep(0.1)
    self.unexpected_messages = []
    if not self._are_expected_delivered():
      raise AssertionError("Expected messages were not delivered: " + str(self.expected_messages))

  def _are_expected_delivered(self):
    return self.expected_messages == []


  @staticmethod
  def _check_message(expected, message):
    (expected_routing_key, expected_pattern) = expected
    (message_routing_key, message_body) = message
    if expected_routing_key != message_routing_key:
      return False
    else:
      return json_pattern_utils.is_contained(expected_pattern, message_body)

  @staticmethod
  def _read_content(path):
    with open(path, 'r') as f:
      content = f.read()
    return content


if __name__ == "__main__":
  InteractiveTestsClient()
