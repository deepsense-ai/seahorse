angular.module('ds.lab').constant('config', {
  'apiHost': 'http://172.28.128.100',
  'apiPort': '9080',
  'apiVersion': '${API_VERSION}',
  'docsHost': '${DOCS_ADDRESS}',
  'notebookHost': 'http://172.28.128.100:8888',
  'editorVersion': '${EDITOR_VERSION}',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://172.28.128.100:15674/',
  'socketReconnectionInterval': 1000
});
