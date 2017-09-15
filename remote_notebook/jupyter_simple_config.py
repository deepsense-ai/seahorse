# Copyright (c) 2015, CodiLime Inc.

import os


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
