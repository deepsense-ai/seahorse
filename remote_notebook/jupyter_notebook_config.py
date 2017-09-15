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

workflow_manager_user = ""
if 'WM_AUTH_USER' in os.environ:
    workflow_manager_user = os.environ['WM_AUTH_USER']

workflow_manager_pass = ""
if 'WM_AUTH_PASS' in os.environ:
    workflow_manager_pass = os.environ['WM_AUTH_PASS']

c.NotebookApp.contents_manager_class = WMContentsManager
c.WMContentsManager.workflow_manager_url = workflow_manager_url
c.WMContentsManager.workflow_manager_user = workflow_manager_user
c.WMContentsManager.workflow_manager_pass = workflow_manager_pass


c.NotebookApp.server_extensions = [
  'headless_notebook_handler.headless_notebook_handler'
]

c.Exporter.preprocessors = ['execute_saver.ExecuteSaver']
c.ClearOutputPreprocessor.enabled = True
c.ExecutePreprocessor.enabled = True
c.ExecutePreprocessor.timeout = -1
#c.coalesce_streams.enabled = True
c.SVG2PDFPreprocessor.enabled = True
c.CSSHTMLHeaderPreprocessor.enabled = True
c.LatexPreprocessor.enabled = True
c.HighlightMagicsPreprocessor.enabled = True
c.ExtractOutputPreprocessor.enabled = True
