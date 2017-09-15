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

    class Path(object):
        class DeserializationFailed(Exception):
            def __init__(self, path):
                super(WMContentsManager.Path.DeserializationFailed, self).__init__(
                        "Deserialization of Path '{}' failed".format(path))

        IDS_PARAMS_SEPARATOR = '___'

        def __init__(self, workflow_id, node_id, params):
            self.workflow_id = workflow_id
            self.node_id = node_id
            self.params = params

        def serialize(self):
            return self.workflow_id + "/" + self.node_id + self.IDS_PARAMS_SEPARATOR + self.params

        @classmethod
        def deserialize(cls, str_path):
            assert isinstance(str_path, basestring)

            uuid_pattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
            m = re.match(".*({})/({}){}(.*)".format(uuid_pattern, uuid_pattern, cls.IDS_PARAMS_SEPARATOR), str_path)
            if m is None or len(m.groups()) < 3:
                raise cls.DeserializationFailed(str_path)

            return cls(*m.group(1, 2, 3))

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

    def _get_wm_notebook_url(self, path):
        return "{}/v1/workflows/{}/notebook/{}".format(
                self.workflow_manager_url, path.workflow_id, path.node_id)

    def create_model(self, content_json, path):
        return {
            "name": "Seahorse Editor Notebook",
            "path": path.serialize(),
            "type": "notebook",
            "writable": True,
            "last_modified": DUMMY_CREATED_DATE,
            "created": DUMMY_CREATED_DATE,
            "content": reads(content_json, NBFORMAT_VERSION) if content_json is not None else None,
            "format": "json" if content_json is not None else None,
            "mimetype": None,
        }

    def _create_notebook(self):
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

    def _save_notebook(self, path, content_json, return_content=False):
        try:
            response = urllib2.urlopen(self._get_wm_notebook_url(path), content_json.encode("utf-8"))
            if response.getcode() == 201:
                return self.create_model(content_json if return_content else None, path)
            else:
                raise web.HTTPError(response.status, response.msg)
        except web.HTTPError:
            raise
        except urllib2.HTTPError as e:
            raise web.HTTPError(e.code, e.msg)
        except Exception as e:
            raise web.HTTPError(500, str(e))

    def get(self, path, content=True, type=None, format=None):
        assert isinstance(path, basestring)
        try:
            path = self.Path.deserialize(path)
        except self.Path.DeserializationFailed as e:
            raise web.HTTPError(400, e.message)

        try:
            response = urllib2.urlopen(self._get_wm_notebook_url(path))
            if response.getcode() == 200:
                content_json = response.read().decode("utf-8")
                return self.create_model(content_json if content else None, path)
            else:
                raise web.HTTPError(response.status, response.msg)
        except web.HTTPError:
            raise
        except urllib2.HTTPError as e:
            if e.code == 404:
                content_json = writes(from_dict(self._create_notebook()), NBFORMAT_VERSION)
                return self._save_notebook(path, content_json, content)
            else:
                raise web.HTTPError(e.code, e.msg)
        except Exception as e:
            raise web.HTTPError(500, str(e))

    def save(self, model, path):
        assert isinstance(path, basestring)
        try:
            path = self.Path.deserialize(path)
        except self.Path.DeserializationFailed as e:
            raise web.HTTPError(400, e.message)

        if model['type'] != "notebook":
            model['message'] = "Cannot save object of type: {}".format(model['type'])
            return model

        content_json = writes(from_dict(model['content']), NBFORMAT_VERSION)
        return self._save_notebook(path, content_json, False)

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
