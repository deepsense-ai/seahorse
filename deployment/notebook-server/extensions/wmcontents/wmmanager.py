# Copyright (c) 2015, CodiLime Inc.

from notebook.services.contents.manager import ContentsManager
from tornado import web
from nbformat import reads, writes, from_dict
from traitlets import Unicode
from datetime import datetime
from .wmcheckpoints import WMCheckpoints
import urllib
import re

NBFORMAT_VERSION = 4
DUMMY_CREATED_DATE = datetime.fromtimestamp(0)

class WMContentsManager(ContentsManager):
    workflow_manager_url = Unicode(
        default_value="http://localhost:9080",
        allow_none=False,
        config=True,
        help="Workflow Manager URL",
    )

    def _checkpoints_class_default(self):
        return WMCheckpoints

    def get_workflow_id(self, path):
        m = re.match(".*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\.ipynb[\?.*]?", path)
        if m is None:
            return None
        return m.group(1)

    def get_wm_notebook_url(self, workflow_id):
        url_format = "{}/v1/workflows/{}/notebook"
        return url_format.format(self.workflow_manager_url, workflow_id)

    def create_model(self, content_json, workflow_id):
        return {
            "name": workflow_id,
            "path": workflow_id + ".ipynb",
            "type": "notebook",
            "writable": True,
            "last_modified": DUMMY_CREATED_DATE,
            "created": DUMMY_CREATED_DATE,
            "content": reads(content_json, NBFORMAT_VERSION) if content_json is not None else None,
            "format": "json" if content_json is not None else None,
            "mimetype": None,
        }

    def get(self, path, content=True, type=None, format=None):
        workflow_id = self.get_workflow_id(path)
        if workflow_id is None:
            raise web.HTTPError(400, "invalid path")

        try:
            notebook_url = self.get_wm_notebook_url(workflow_id)
            response = urllib.request.urlopen(notebook_url)
            if response.status == 200:
                content_json = response.read().decode("utf-8")
                return self.create_model(content_json if content else None, workflow_id)
            else:
                raise web.HTTPError(response.status, response.msg)
        except web.HTTPError:
            raise
        except Exception as e:
            raise web.HTTPError(500, str(e))

    def save(self, model, path):
        workflow_id = self.get_workflow_id(path)
        if workflow_id is None:
            raise web.HTTPError(400, "invalid path")

        if model['type'] != "notebook":
            model['message'] = "Cannot save object of type: {}".format(model['type'])
            return model

        notebook_url = self.get_wm_notebook_url(workflow_id)
        content_json = writes(from_dict(model['content']), NBFORMAT_VERSION)
        try:
            response = urllib.request.urlopen(notebook_url, content_json.encode("utf-8"))
            if response.code == 201:
                return self.create_model(None, workflow_id)
            else:
                raise web.HTTPError(response.status, response.msg)
        except web.HTTPError:
            raise
        except Exception as e:
            raise web.HTTPError(500, str(e))

    def delete_file(self, path):
        raise web.HTTPError(400, "Unsupported: delete_file {}".format(path))

    def rename_file(self, old_path, path):
        raise web.HTTPError(400, "Unsupported: rename_file {} {}".format(old_path, path))

    def file_exists(self, path):
        try:
            self.get(path)
            return True
        except:
            return False

    def dir_exists(self, path):
        return False

    def is_hidden(self, path):
        return False
