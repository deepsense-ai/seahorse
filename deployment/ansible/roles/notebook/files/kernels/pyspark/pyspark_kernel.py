# Copyright (c) 2015, CodiLime Inc.

from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type
from gateway_resolver import GatewayResolver

import json
import os
import re
import sys
import urllib2


class PySparkKernel(IPythonKernel):

    def __init__(self, *args, **kwargs):
        super(PySparkKernel, self).__init__(**kwargs)

        kernel_id = extract_kernel_id(sys.argv[2])
        (workflow_id, dataframe_owner_node_id, port_number) = _extract_dataframe_source(kernel_id)
        gateway_resolver = GatewayResolver([os.environ['RABBIT_MQ_ADDRESS'],
                                            int(os.environ['RABBIT_MQ_PORT'])])

        self._insert_metadata(kernel_id)

        self._insert_gateway_address(
            gateway_resolver.get_gateway_address())

        self._insert_dataframe_source(workflow_id, dataframe_owner_node_id, port_number)

        self._initialize_pyspark()

    def _insert_metadata(self, kernel_id):
        self.do_execute('kernel_id = "{}"'.format(kernel_id), False)
        self.do_execute('notebook_server_address = "{}"'.format(
            os.environ['NOTEBOOK_SERVER_ADDRESS']), False)
        self.do_execute('notebook_server_port = {}'.format(
            os.environ['NOTEBOOK_SERVER_PORT']), False)
        self.do_execute('mq_address = "{}"'.format(os.environ['RABBIT_MQ_ADDRESS']), False)
        self.do_execute('mq_port = {}'.format(os.environ['RABBIT_MQ_PORT']), False)

    def _insert_gateway_address(self, gateway_address):
        host, port = gateway_address
        self.do_execute('gateway_address = "{}"'.format(host), False)
        self.do_execute('gateway_port = {}'.format(port), False)

    def _insert_dataframe_source(self, workflow_id, dataframe_owner_node_id, port_number):
        self.do_execute('workflow_id = "{}"'.format(workflow_id), False)
        self.do_execute('port_number = {}'.format(port_number), False)

        if dataframe_owner_node_id:
            self.do_execute('node_id = "{}"'.format(dataframe_owner_node_id), False)
        else:
            self.do_execute('node_id = None', False)

    def _initialize_pyspark(self):
        self.do_execute(open(os.environ['KERNEL_INIT_FILE']).read(), False)


def extract_kernel_id(connection_file_name):
    m = re.search('kernel-([0-9a-f\-]+)', connection_file_name)
    return m.group(1)


def _extract_dataframe_source(kernel_id):
    notebook_server_location = os.environ['NOTEBOOK_SERVER_ADDRESS'] + ":" + os.environ['NOTEBOOK_SERVER_PORT']
    response = urllib2.urlopen("http://" + notebook_server_location + "/jupyter/api/sessions").read()
    sessions = json.loads(response)
    for session in sessions:
        if session['kernel']['id'] == kernel_id:
            return _extract_dataframe_source_from_path(session['notebook']['path'])

    raise Exception('Workflow matching kernel ID ' + kernel_id + 'was not found.')


def _extract_dataframe_source_from_path(notebook_id):
    ids_params_separator = '___'
    ids_separator = '/'
    node_id_port_separator = ','

    notebook_id_data, datasource_data = tuple(notebook_id.split(ids_params_separator))

    workflow_id, notebook_node_id = tuple(notebook_id_data.split(ids_separator))

    dataframe_owner_node_id, output_port_number = None, None
    if len(datasource_data) > 0:
        dataframe_owner_node_id, output_port_number = datasource_data.split(node_id_port_separator)
        output_port_number = int(output_port_number)

    return workflow_id, dataframe_owner_node_id, output_port_number


class PySparkKernelApp(IPKernelApp):
    kernel_class = Type('pyspark_kernel.PySparkKernel', config=True, klass='ipykernel.ipkernel.IPythonKernel')

if __name__ == '__main__':
    app = PySparkKernelApp.instance()
    app.initialize()
    app.start()
