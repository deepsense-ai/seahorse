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



import os
import copy
from nbconvert.exporters.export import exporter_map
from nbconvert.writers.files import FilesWriter
from nbconvert.preprocessors import ExecutePreprocessor
from notebook.base.handlers import IPythonHandler
from notebook.utils import url_path_join
from tornado import web, escape
from tornado.concurrent import run_on_executor
from concurrent.futures import ThreadPoolExecutor   # `pip install futures` for python2

from seahorse_notebook_path import SeahorseNotebookPath


class HeadlessNotebookHandler(IPythonHandler):
    executor = ThreadPoolExecutor(max_workers=10)

    @run_on_executor
    def process_notebook(self, path):
        Exporter = exporter_map["html"]
        exporter = Exporter(config=self.config, log=self.log)
        serialized_path = path.serialize()

        # We get ExecutePreprocessor from exporter list
        # and add notebook path so kernel can use the path for various operations.
        # For example it can load the notebook from url
        ep = next(filter(lambda c: isinstance(c, ExecutePreprocessor), exporter._preprocessors))
        ep.extra_arguments.append("--seahorse_notebook_path=" + serialized_path)

        model = self.contents_manager.get(path=serialized_path)
        try:
            output, resources = exporter.from_notebook_node(model['content'])

            model['content'] = resources["seahorse_notebook_content"]
            self.contents_manager.save(model, path=serialized_path)

            resources['output_extension'] = ''
            FilesWriter(config=self.config, log=self.log).write(
                output, resources,
                notebook_name=HeadlessNotebookHandler.notebook_name(path.workflow_id, path.node_id))
        except Exception as e:
            raise web.HTTPError(500, "nbconvert failed: %s" % e)

    # get HTML-ized un-editable notebook
    def get(self, seahorse_notebook_path):
        Exporter = exporter_map["html"]
        updated_config = self.no_execution_config(self.config)
        exporter = Exporter(config=updated_config, log=self.log)
        model = self.contents_manager.get(path=seahorse_notebook_path)

        try:
            output, _ = exporter.from_notebook_node(model['content'])
            self.write(output)
        except Exception as e:
            raise web.HTTPError(500, "nbconvert failed: %s" % e)

    def post(self):
        data = escape.json_decode(self.request.body)
        workflow_id, node_id, language = data["workflow_id"], data["node_id"], data["language"]
        try:
            os.remove(self.notebook_name(workflow_id, node_id))
        except FileNotFoundError:
            pass

        # use input dataframe for headless
        seahorse_notebook_path = SeahorseNotebookPath(workflow_id, node_id, language, node_id, 0)
        self.process_notebook(seahorse_notebook_path)
        raise web.HTTPError(202)

    @staticmethod
    def notebook_storage_folder():
        return "/home/jovyan/work/"

    @staticmethod
    def notebook_name(workflow_id, node_id):
        return "{0}{1}_{2}.html".format(
            HeadlessNotebookHandler.notebook_storage_folder(), workflow_id, node_id)

    @staticmethod
    def no_execution_config(config):
        new_config = copy.deepcopy(config)
        new_config.ClearOutputPreprocessor.enabled = False
        new_config.ExecutePreprocessor.enabled = False
        return new_config

def load_jupyter_server_extension(nb_server_app):
    """
    Called when the extension is loaded.

    Args:
        nb_server_app (NotebookWebApplication): handle to the Notebook webserver instance.
    """
    web_app = nb_server_app.web_app
    host_pattern = '.*$'
    base_url = web_app.settings['base_url']
    route_pattern = url_path_join(base_url, '/HeadlessNotebook')

    web_app.add_handlers(host_pattern, [(route_pattern, HeadlessNotebookHandler)])

    # regex excludes dot character to prevent '/OfflineNotebook/workflowid/nodeid/custom.css' from being processed
    web_app.add_handlers(host_pattern, [(url_path_join(base_url,
        '/OfflineNotebook/(?P<seahorse_notebook_path>[^.]+)'), HeadlessNotebookHandler)])

    route_pattern_with_workflow_id = url_path_join(base_url, '/HeadlessNotebook/([^/]+)')
    web_app.add_handlers(host_pattern,
                         [(route_pattern_with_workflow_id, web.StaticFileHandler, {"path": "/home/jovyan/work/"})])
