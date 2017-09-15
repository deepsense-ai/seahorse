# Copyright (c) 2015, CodiLime Inc.

from notebook.services.contents.manager import ContentsManager
from tornado import web
from nbformat import reads, writes, from_dict
from traitlets import Unicode
from datetime import datetime
import urllib2
from .wmcheckpoints import WMCheckpoints
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

    kernel_name = Unicode(
        default_value="pyspark",
        allow_none=False,
        config=True,
        help="Default kernel name",
    )

    kernel_display_name = Unicode(
        default_value="Python (Spark)",
        allow_none=False,
        config=True,
        help="Default kernel display name",
    )

    kernel_python_version = Unicode(
        default_value="2.7.10",
        allow_none=False,
        config=True,
        help="Default kernel python version",
    )

    def _checkpoints_class_default(self):
        return WMCheckpoints

    def get_notebook_id(self, path):
        uuid_pattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        m = re.match(".*({})\/({})[\?.*]?".format(uuid_pattern, uuid_pattern), path)
        if m is None:
            return None
        return (m.group(1), m.group(2))

    def get_wm_notebook_url(self, workflow_id, node_id):
        url_format = "{}/v1/workflows/{}/notebook/{}"
        return url_format.format(self.workflow_manager_url, workflow_id, node_id)

    def create_model(self, content_json, workflow_id, node_id):
        return {
            "name": "Seahorse Editor Notebook",
            "path": workflow_id + "/" + node_id,
            "type": "notebook",
            "writable": True,
            "last_modified": DUMMY_CREATED_DATE,
            "created": DUMMY_CREATED_DATE,
            "content": reads(content_json, NBFORMAT_VERSION) if content_json is not None else None,
            "format": "json" if content_json is not None else None,
            "mimetype": None,
        }

    def create_notebook(self):
        return {
            "cells": [],
            "metadata": {
                "kernelspec": {
                    "display_name": self.kernel_display_name,
                    "language": "python",
                    "name": self.kernel_name
                },
                "language_info": {
                    "name": "python",
                    "version": self.kernel_python_version
                }
            },
            "nbformat": NBFORMAT_VERSION,
            "nbformat_minor": 0
        }

    def save_notebook(self, workflow_id, node_id, content_json, return_content=False):
        notebook_url = self.get_wm_notebook_url(workflow_id, node_id)

        try:
            response = urllib2.urlopen(notebook_url, content_json.encode("utf-8"))
            if response.getcode() == 201:
                return self.create_model(content_json if return_content else None, workflow_id, node_id)
            else:
                raise web.HTTPError(response.status, response.msg)
        except web.HTTPError:
            raise
        except urllib2.HTTPError as e:
            raise web.HTTPError(e.code, e.msg)
        except Exception as e:
            raise web.HTTPError(500, str(e))

    def get(self, path, content=True, type=None, format=None):
        notebook_id = self.get_notebook_id(path)
        if notebook_id is None:
            raise web.HTTPError(400, "Invalid path")
        (workflow_id, node_id) = notebook_id

        try:
            notebook_url = self.get_wm_notebook_url(workflow_id, node_id)
            response = urllib2.urlopen(notebook_url)
            if response.getcode() == 200:
                content_json = response.read().decode("utf-8")
                return self.create_model(content_json if content else None, workflow_id, node_id)
            else:
                raise web.HTTPError(response.status, response.msg)
        except web.HTTPError:
            raise
        except urllib2.HTTPError as e:
            if e.code == 404:
                content_json = writes(from_dict(self.create_notebook()), NBFORMAT_VERSION)
                return self.save_notebook(workflow_id, node_id, content_json, content)
            else:
                raise web.HTTPError(e.code, e.msg)
        except Exception as e:
            raise web.HTTPError(500, str(e))

    def save(self, model, path):
        notebook_id = self.get_notebook_id(path)
        if notebook_id is None:
            raise web.HTTPError(400, "Invalid path")
        (workflow_id, node_id) = notebook_id

        if model['type'] != "notebook":
            model['message'] = "Cannot save object of type: {}".format(model['type'])
            return model

        content_json = writes(from_dict(model['content']), NBFORMAT_VERSION)
        return self.save_notebook(workflow_id, node_id, content_json, False)

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
