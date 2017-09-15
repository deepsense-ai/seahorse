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


class SeahorseNotebookPath(object):
    class DeserializationFailed(Exception):
        def __init__(self, path):
            super(SeahorseNotebookPath.DeserializationFailed, self).__init__(
                    "Deserialization of Path '{}' failed".format(path))

    def __init__(self, workflow_id, node_id, language, datasource_node_id=None, datasource_node_port=None):
        self.workflow_id = workflow_id
        self.node_id = node_id
        self.language = language
        self.datasource_node_id = datasource_node_id
        self.datasource_node_port = datasource_node_port

    def serialize(self):
        return '/'.join([self.workflow_id, self.node_id, self.params()])

    @classmethod
    def deserialize(cls, seahorse_notebook_path):
        assert isinstance(seahorse_notebook_path, str)
        if seahorse_notebook_path.startswith('/'):
            seahorse_notebook_path = seahorse_notebook_path[1:]
        try:
            workflow_id, node_id, params = seahorse_notebook_path.split('/')
            deserialized_params = json.loads(base64.decodestring(params.encode()).decode('utf-8'))
            # If nothing is connected to the Notebook node, we don't expect a source
            if 'dataframeSource' in deserialized_params and len(deserialized_params['dataframeSource']) > 0:
                dataframe_owner_node_id = deserialized_params['dataframeSource']['nodeId']
                output_port_number = deserialized_params['dataframeSource']['port']
            else:
                dataframe_owner_node_id = None
                output_port_number = None
            return cls(workflow_id, node_id, deserialized_params['language'], dataframe_owner_node_id,
                       output_port_number)
        except ValueError:
            raise cls.DeserializationFailed(seahorse_notebook_path)

    def params(self):
        deserialized_params = {}
        deserialized_params['language'] = self.language
        if self.datasource_node_id is not None and self.datasource_node_port is not None:
            deserialized_params['dataframeSource'] = {}
            deserialized_params['dataframeSource']['nodeId'] = self.datasource_node_id
            deserialized_params['dataframeSource']['port'] = self.datasource_node_port
        js = json.dumps(deserialized_params)
        params = base64.b64encode(js.encode('ascii'))
        return params.decode('utf-8')
