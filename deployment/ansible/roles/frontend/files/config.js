angular.module('ds.lab').constant('config', {
  'apiHost': '${PROXY_HOST}',
  'apiPort': '${PROXY_PORT}',
  'apiVersion': '${API_VERSION}',
  'editorVersion': '${EDITOR_VERSION}',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': '10000'
});
