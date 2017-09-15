# Copyright (c) 2016, CodiLime Inc.

import json
import urllib2
from utils import debug

class NotebookServerClient(object):

    def __init__(self, nb_host, nb_port, kernel_id):
        self._nb_host = nb_host
        self._nb_port = nb_port
        self._kernel_id = kernel_id
        self._notebook_server_location = "{}:{}".format(self._nb_host, self._nb_port)
        self._api_url = "http://{}/jupyter/api/sessions".format(self._notebook_server_location)

    def extract_dataframe_source(self):
        session = self._get_my_session()
        return self._extract_dataframe_source_from_path(session['notebook']['path'])

    def restart_kernel(self):
        # 'data' specified to make it a POST request
        urllib2.urlopen("http://{}/jupyter/api/kernels/{}/restart".format(self._notebook_server_location,
                                                                          self._kernel_id), "")

    def stop_kernel(self):
        debug("NotebookServerClient::stop_kernel: getting session")
        session = self._get_my_session()
        debug("NotebookServerClient::stop_kernel: got session: {}".format(session))
        url = "{}/{}".format(self._api_url, session['id'])
        debug("NotebookServerClient::stop_kernel: preparing DELETE request to {}".format(url))
        request = urllib2.Request(url)
        request.get_method = lambda: 'DELETE'
        result = urllib2.urlopen(request)
        debug("NotebookServerClient::stop_kernel: DELETE returned: {}".format(result))

    def _get_my_session(self):
        sessions = self._get_sessions()
        for session in sessions:
            if session['kernel']['id'] == self._kernel_id:
                return session

        raise Exception('Session matching kernel ID ' + self._kernel_id + 'was not found.')

    def _get_sessions(self):
        response = urllib2.urlopen(self._api_url).read()
        return json.loads(response)

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
