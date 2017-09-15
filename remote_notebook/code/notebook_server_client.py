# Copyright (c) 2016, CodiLime Inc.

import json
import base64
import urllib2
from utils import Logging


class NotebookServerClient(Logging):

    def __init__(self, nb_host, nb_port, kernel_id):
        super(NotebookServerClient, self).__init__()
        self._nb_host = nb_host
        self._nb_port = nb_port
        self._kernel_id = kernel_id
        self._notebook_server_location = "{}:{}".format(self._nb_host, self._nb_port)
        self._api_url = "http://{}/jupyter/api/sessions".format(self._notebook_server_location)

    def extract_dataframe_source(self):
        session = self._get_my_session()
        return self._extract_notebook_params_from_path(session['notebook']['path'])

    def restart_kernel(self):
        # 'data' specified to make it a POST request
        urllib2.urlopen("http://{}/jupyter/api/kernels/{}/restart".format(self._notebook_server_location,
                                                                          self._kernel_id), "")

    def stop_kernel(self):
        self.logger.debug("Getting session")
        session = self._get_my_session()
        self.logger.debug("Got session: {}".format(session))
        url = "{}/{}".format(self._api_url, session['id'])
        self.logger.debug("Preparing DELETE request to {}".format(url))
        request = urllib2.Request(url)
        request.get_method = lambda: 'DELETE'
        result = urllib2.urlopen(request)
        self.logger.debug("DELETE returned: {}".format(result))

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
    def _extract_notebook_params_from_path(notebook_path):
        workflow_id, _, params = notebook_path.split('/')
        params = json.loads(base64.decodestring(params))

        # If nothing is connected to the Notebook node, we don't expect a source
        if len(params['dataframeSource']) == 0:
            return workflow_id, None, None

        dataframe_owner_node_id = params['dataframeSource']['nodeId']
        output_port_number = params['dataframeSource']['port']
        return workflow_id, dataframe_owner_node_id, output_port_number

