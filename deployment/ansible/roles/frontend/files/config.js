angular.module('ds.lab').constant('config', {
  'apiHost': 'http://172.28.128.100',
  'apiPort': '8000',
  'apiVersion': '${API_VERSION}',
  'docsHost': '${DOCS_ADDRESS}',
  'notebookHost': 'http://172.28.128.100:8000/jupyter',
  'editorVersion': '${EDITOR_VERSION}',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://172.28.128.100:8000/',
  'socketReconnectionInterval': 1000
});
