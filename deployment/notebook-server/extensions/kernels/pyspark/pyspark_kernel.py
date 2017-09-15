# Copyright (c) 2015, CodiLime Inc.

from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type

import json
import os
import pika
import re
import sys
import urllib2


class PySparkKernel(IPythonKernel):
    def __init__(self, *args, **kwargs):
        super(PySparkKernel, self).__init__(*args, **kwargs)
        self.kernel_id = extract_kernel_id(sys.argv[2])
        self.workflow_id = get_workflow_id(self.kernel_id)
        (gateway_address, gateway_port) = get_gateway_location(self.workflow_id)
        self._insert_gateway_location(gateway_address, gateway_port)
        self._initialize_pyspark()

    def _insert_gateway_location(self, gateway_address, gateway_port):
        self.do_execute("gateway_address = \"" + gateway_address + "\"", False)
        self.do_execute("gateway_port = " + str(gateway_port) + "", False)

    def _initialize_pyspark(self):
        self.do_execute(open(os.environ['KERNEL_INIT_FILE']).read(), False)


def extract_kernel_id(connection_file_name):
    m = re.search('kernel-([0-9a-f\-]+)', connection_file_name)
    return m.group(1)


def get_workflow_id(kernel_id):
    notebook_server_location = os.environ['NOTEBOOK_SERVER_ADDRESS'] + ":" + os.environ['NOTEBOOK_SERVER_PORT']
    response = urllib2.urlopen("http://" + notebook_server_location + "/api/sessions").read()
    sessions = json.loads(response)
    for session in sessions:
        if session['kernel']['id'] == kernel_id:
            return session['notebook']['path']
    raise Exception('Workflow matching kernel ID ' + kernel_id + 'was not found.')


def get_gateway_location(workflow_id):
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            os.environ['RABBIT_MQ_ADDRESS'],
            int(os.environ['RABBIT_MQ_PORT'])))
    request_queue_name = 'gateway_requests'
    response_queue_name = 'gateway_responses'

    channel = connection.channel()
    channel.queue_declare(queue=request_queue_name)
    channel.queue_declare(queue=response_queue_name)

    request_body = {
        "messageType": "getGatewayAddress",
        "messageBody": {
            "workflowId": workflow_id
        }
    }

    channel.basic_publish(exchange='', routing_key=request_queue_name, body=json.dumps(request_body))

    gateway_location = [None, None]

    def receive_gateway_location_response(ch, method, properties, body):
        response = json.loads(body)
        gateway_location[0] = response['messageBody']['hostname']
        gateway_location[1] = response['messageBody']['port']
        channel.stop_consuming()

    channel.basic_consume(receive_gateway_location_response, queue=response_queue_name)
    channel.start_consuming()
    connection.close()

    return tuple(gateway_location)


class PySparkKernelApp(IPKernelApp):
    kernel_class = Type('pyspark_kernel.PySparkKernel', config=True, klass='ipykernel.ipkernel.IPythonKernel')

if __name__ == '__main__':
    app = PySparkKernelApp.instance()
    app.initialize()
    app.start()