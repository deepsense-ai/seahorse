'use strict';

// THIS FILE IS TEMPLATED IN BUILD PROCESS
// CHANGES HERE WILL BE LOST IN BUILD IF NOT PROPAGATED TO BUILD PIPELINE.

angular.module('ds.lab').constant('config', {
  // 172.28.128.100 - vagrant address
  'apiHost': 'http://172.28.128.100',
  'docsHost': 'https://seahorse.deepsense.io',
  'notebookHost': 'http://172.28.128.100:8888',
  'apiPort': '9080',
  'apiVersion': '1.1.0',
  'editorVersion': '1.1.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://172.28.128.100:15674/',
  'socketReconnectionInterval': 1000,
  'debugInfoEnabled': true
});
