# Copyright (c) 2016, CodiLime Inc.

import base64
import json
import urllib2

from utils import Logging
from seahorse_notebook_path import SeahorseNotebookPath

class NotebookServerClient(Logging):
    def __init__(self, nb_host, nb_port, kernel_id, seahorse_notebook_path=None):
        super(NotebookServerClient, self).__init__()
        self._nb_host = nb_host
        self._nb_port = nb_port
        self._kernel_id = kernel_id
        self._notebook_server_location = "{}:{}".format(self._nb_host, self._nb_port)
        self._api_url = "http://{}/jupyter/api/sessions".format(self._notebook_server_location)
        self.seahorse_notebook_path = seahorse_notebook_path

    def _get_path(self):
        if self.seahorse_notebook_path:
            return self.seahorse_notebook_path
        else:
            session = self._get_my_session()
            return str(session['notebook']['path'])

    def extract_dataframe_source(self):
        notebook_path = SeahorseNotebookPath.deserialize(self._get_path())
        return notebook_path.workflow_id, notebook_path.datasource_node_id, notebook_path.datasource_node_port

    def restart_kernel(self):
        # 'data' specified to make it a POST request
        urllib2.urlopen("http://{}/jupyter/api/kernels/{}/restart".format(self._notebook_server_location,
                                                                          self._kernel_id), "")

    def stop_kernel(self):
        if self.seahorse_notebook_path is not None:
            return
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


