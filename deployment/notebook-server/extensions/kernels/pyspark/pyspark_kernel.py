# Copyright (c) 2015, CodiLime Inc.
from threading import Thread

import time
from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type

import json
import os
import pika
import re
import sys
import urllib2


SEAHORSE_MQ_EXCHANGE = 'seahorse'
TO_EXECUTOR_TOPIC = 'to_executor'
TO_KERNEL_TOPIC = 'to_kernel'


class PySparkKernel(IPythonKernel):

    def __init__(self, *args, **kwargs):
        super(PySparkKernel, self).__init__(**kwargs)

        kernel_id = extract_kernel_id(sys.argv[2])
        (workflow_id, node_id) = get_notebook_id(kernel_id)
        gateway_resolver = GatewayResolver(workflow_id)

        self._insert_gateway_address(
            gateway_resolver.get_gateway_address())

        self._insert_workflow_id(workflow_id, node_id)

        self._initialize_pyspark()

    def _insert_gateway_address(self, gateway_address):
        host, port = gateway_address
        self.do_execute('gateway_address = "{}"'.format(host), False)
        self.do_execute('gateway_port = {}'.format(port), False)

    def _insert_workflow_id(self, workflow_id, node_id):
        self.do_execute('workflow_id = "{}"'.format(workflow_id), False)
        self.do_execute('node_id = "{}"'.format(node_id), False)

    def _initialize_pyspark(self):
        self.do_execute(open(os.environ['KERNEL_INIT_FILE']).read(), False)


def extract_kernel_id(connection_file_name):
    m = re.search('kernel-([0-9a-f\-]+)', connection_file_name)
    return m.group(1)


def get_notebook_id(kernel_id):
    notebook_server_location = os.environ['NOTEBOOK_SERVER_ADDRESS'] + ":" + os.environ['NOTEBOOK_SERVER_PORT']
    response = urllib2.urlopen("http://" + notebook_server_location + "/api/sessions").read()
    sessions = json.loads(response)
    for session in sessions:
        if session['kernel']['id'] == kernel_id:
            return tuple(session['notebook']['path'].split("/"))
    raise Exception('Workflow matching kernel ID ' + kernel_id + 'was not found.')


class GatewayResolver(object):
    def __init__(self, workflow_id):
        self.workflow_id = workflow_id

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
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(os.environ['RABBIT_MQ_ADDRESS'],
                                      int(os.environ['RABBIT_MQ_PORT'])))

        channel = connection.channel()
        channel.exchange_declare(exchange=SEAHORSE_MQ_EXCHANGE,
                                 exchange_type='direct')
        queue_name = channel.queue_declare(exclusive=True).method.queue

        channel.queue_bind(exchange=SEAHORSE_MQ_EXCHANGE,
                           queue=queue_name,
                           routing_key=TO_KERNEL_TOPIC)

        address = self.Address()

        def keep_sending():
            return not address.received()

        request_sender_thread = Thread(target=lambda: self._request_sender(channel, keep_sending))
        request_sender_thread.daemon = True
        request_sender_thread.start()

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
            "messageType": "getPythonGatewayAddress",
            "messageBody": {
                "workflowId": self.workflow_id
            }
        }

        while keep_sending():
            channel.basic_publish(exchange=SEAHORSE_MQ_EXCHANGE,
                                  routing_key=TO_EXECUTOR_TOPIC,
                                  body=json.dumps(get_python_gateway_address))
            time.sleep(1)


class PySparkKernelApp(IPKernelApp):
    kernel_class = Type('pyspark_kernel.PySparkKernel', config=True, klass='ipykernel.ipkernel.IPythonKernel')

if __name__ == '__main__':
    app = PySparkKernelApp.instance()
    app.initialize()
    app.start()
