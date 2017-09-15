# Copyright (c) 2016, CodiLime Inc.


import os
from nbconvert.exporters.export import exporter_map
from nbconvert.writers.files import FilesWriter
from notebook.base.handlers import IPythonHandler
from notebook.utils import url_path_join
from tornado import web, escape
from tornado.concurrent import run_on_executor
from concurrent.futures import ThreadPoolExecutor   # `pip install futures` for python2

from seahorse_notebook_path import SeahorseNotebookPath


class HeadlessNotebookHandler(IPythonHandler):
    executor = ThreadPoolExecutor(max_workers=4)

    @run_on_executor
    def process_notebook(self, path):
        Exporter = exporter_map["pdf"]
        exporter = Exporter(config=self.config, log=self.log)
        serialized_path = path.serialize()
        for p in exporter._preprocessors:
            if hasattr(p, 'extra_arguments'):
                p.extra_arguments.append("--seahorse_notebook_path=" + serialized_path)

        model = self.contents_manager.get(path=serialized_path)
        try:
            output, resources = exporter.from_notebook_node(model['content'])
            resources['output_extension'] = ''
            writer = FilesWriter(config=self.config, log=self.log)
            model['content'] = resources["seahorse_notebook_content"]
            self.contents_manager.save(model, path=serialized_path)

            write_results = writer.write(
                output, resources, notebook_name="/home/jovyan/work/" + path.workflow_id + "_" + path.node_id + '.pdf')
            self.log.error(write_results)
        except Exception as e:
            raise web.HTTPError(500, "nbconvert failed: %s" % e)

    def post(self):
        data = escape.json_decode(self.request.body)
        workflow_id, node_id, language = data["workflow_id"], data["node_id"], data["language"]
        try:
            os.remove("/home/jovyan/work/" + workflow_id + "_" + node_id + ".pdf")
        except FileNotFoundError:
            pass

        # use input dataframe for headless
        seahorse_notebook_path = SeahorseNotebookPath(workflow_id, node_id, language, node_id, 0)
        self.process_notebook(seahorse_notebook_path)
        raise web.HTTPError(201)


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
    route_pattern_with_workflow_id = url_path_join(base_url, '/HeadlessNotebook/([^/]+)')
    web_app.add_handlers(host_pattern,
                         [(route_pattern_with_workflow_id, web.StaticFileHandler, {"path": "/home/jovyan/work/"})])
