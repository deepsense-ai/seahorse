# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import base64
import json
import urllib2

from utils import Logging
from seahorse_notebook_path import SeahorseNotebookPath


class NotebookServerClient(Logging):
    # Passed seahorse_notebook_path enforces NotebookServerClient to use this path for finding/saving
    # notebook. When seahorse_notebook_path is not defined then NotebookServerClient will connect to session
    # endpoint to find its path
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
        # When seahorse_notebook_path is not defined then no session is on notebook server.
        # We don't have to delete the session.
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


