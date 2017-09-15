# Copyright (c) 2015, CodiLime Inc.

import os

from wmcontents import WMContentsManager

c = get_config()
c.NotebookApp.open_browser = False
c.NotebookApp.port = int(os.environ.get('JUPYTER_LISTENING_PORT', 8888))
c.NotebookApp.ip = os.environ.get('JUPYTER_LISTENING_IP', '127.0.0.1')
if c.NotebookApp.ip == '0.0.0.0':
    c.NotebookApp.ip = '*'
c.NotebookApp.allow_origin = '*'
c.NotebookApp.base_url = '/jupyter/'
c.NotebookApp.tornado_settings = {
    'headers': {
        'Content-Security-Policy': "frame-ancestors 'self' *"
    }
}

c.NotebookApp.contents_manager_class = WMContentsManager
c.WMContentsManager.workflow_manager_url = os.environ.get('WM_URL', 'http://localhost:9080')
c.WMContentsManager.workflow_manager_user = os.environ.get('WM_AUTH_USER', '')
c.WMContentsManager.workflow_manager_pass = os.environ.get('WM_AUTH_PASS', '')

c.NotebookApp.server_extensions = [
  'headless_notebook_handler.headless_notebook_handler'
]

c.Exporter.preprocessors = ['execute_saver.ExecuteSaver']
c.ClearOutputPreprocessor.enabled = True
c.ExecutePreprocessor.enabled = True
c.ExecutePreprocessor.allow_errors = True
c.ExecutePreprocessor.timeout = -1
c.SVG2PDFPreprocessor.enabled = True
c.CSSHTMLHeaderPreprocessor.enabled = True
c.LatexPreprocessor.enabled = True
c.HighlightMagicsPreprocessor.enabled = True

