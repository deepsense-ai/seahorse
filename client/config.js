angular.module('ds.lab').constant('config', {
  'apiHost': 'http://10.5.10.4',
  'docsHost': 'https://seahorse.deepsense.io',
  'notebookHost': 'http://localhost:8888',
  'apiPort': '9080',
  'apiVersion': '0.6.0',
  'editorVersion': '1.0.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://10.5.10.4:15674/',
  'socketReconnectionInterval': 1000,
  'queueRoutes': {
    'connect': '/exchange/seahorse/to_executor',
    'executionStatus': '/exchange/<%= workflowId %>/to_editor',
    'launch/abort': '/exchange/<%= workflowId %>/to_executor'
  }
});
