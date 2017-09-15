# Copyright (c) 2016, CodiLime Inc.

import json
from urllib2 import urlopen


class NotebookServerClient(object):

    def __init__(self, nb_host, nb_port, kernel_id):
        self._nb_host = nb_host
        self._nb_port = nb_port
        self._kernel_id = kernel_id
        self._notebook_server_location = "{}:{}".format(self._nb_host, self._nb_port)

    def extract_dataframe_source(self):
        response = urlopen("http://" + self._notebook_server_location + "/jupyter/api/sessions").read()
        sessions = json.loads(response)
        for session in sessions:
            if session['kernel']['id'] == self._kernel_id:
                return self._extract_dataframe_source_from_path(session['notebook']['path'])

        raise Exception('Workflow matching kernel ID ' + self._kernel_id + 'was not found.')

    def restart_kernel(self):
        # 'data' specified to make it a POST request
        urlopen("http://{}/jupyter/api/kernels/{}/restart".format(self._notebook_server_location, self._kernel_id), "")

    @staticmethod
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
