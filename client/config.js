angular.module('ds.lab').constant('config', {
  'apiHost': 'https://editor.seahorse.deepsense.io',
  'docsHost': 'https://seahorse.deepsense.io',
  'apiPort': '443',
  'apiVersion': '0.5.0',
  'editorVersion': '1.0.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://127.0.0.1:15674/',
  'socketReconnectionInterval': 1000,
  'queueRoutes': {
    'connect': '/exchange/seahorse/to_executor',
    'executionStatus': '/exchange/<%= workflowId %>/to_editor',
    'launch/abort': '/exchange/<%= workflowId %>/to_executor'
  }
});
