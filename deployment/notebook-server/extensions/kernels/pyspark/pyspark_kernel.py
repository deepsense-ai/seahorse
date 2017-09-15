# Copyright (c) 2015, CodiLime Inc.
from threading import Thread

import time
from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type
from gateway_resolver import GatewayResolver

import json
import os
import pika
import re
import sys
import urllib2


class PySparkKernel(IPythonKernel):

    def __init__(self, *args, **kwargs):
        super(PySparkKernel, self).__init__(**kwargs)

        kernel_id = extract_kernel_id(sys.argv[2])
        (workflow_id, node_id) = get_notebook_id(kernel_id)
        gateway_resolver = GatewayResolver([os.environ['RABBIT_MQ_ADDRESS'],
                                            int(os.environ['RABBIT_MQ_PORT'])])

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


class PySparkKernelApp(IPKernelApp):
    kernel_class = Type('pyspark_kernel.PySparkKernel', config=True, klass='ipykernel.ipkernel.IPythonKernel')

if __name__ == '__main__':
    app = PySparkKernelApp.instance()
    app.initialize()
    app.start()
