angular.module('ds.lab').constant('config', {
  'apiHost': '${PUBLIC_WM_ADDRESS}',
  'apiPort': '${PUBLIC_WM_PORT}',
  'apiVersion': '${API_VERSION}',
  'docsHost': '${PUBLIC_DOCS_ADDRESS}',
  'notebookHost': '${NOTEBOOK_ADDRESS}',
  'editorVersion': '${EDITOR_VERSION}',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': '${QUEUE_ADDRESS}',
  'socketReconnectionInterval': 1000
});
