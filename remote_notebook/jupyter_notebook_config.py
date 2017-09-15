# Copyright (c) 2015, CodiLime Inc.

import os

from wmcontents import WMContentsManager

c = get_config()
c.NotebookApp.ip = '*'
c.NotebookApp.port = 8888
c.NotebookApp.open_browser = False

c.NotebookApp.allow_origin = '*'
c.NotebookApp.base_url = '/jupyter/'
c.NotebookApp.tornado_settings = {
    'headers': {
        'Content-Security-Policy': "frame-ancestors 'self' *"
    }
}

workflow_manager_url = "http://localhost:9080"
if 'WM_URL' in os.environ:
    workflow_manager_url = os.environ['WM_URL']

c.NotebookApp.contents_manager_class = WMContentsManager
c.WMContentsManager.workflow_manager_url = workflow_manager_url
c.WMContentsManager.kernel_name = "pyspark"
c.WMContentsManager.kernel_display_name = "Python (Spark)"
c.WMContentsManager.kernel_python_version = "2.7.10"
