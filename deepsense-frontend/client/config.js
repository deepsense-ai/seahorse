'use strict';

// THIS FILE IS TEMPLATED IN BUILD PROCESS
// CHANGES HERE WILL BE LOST IN BUILD IF NOT PROPAGATED TO BUILD PIPELINE.

angular.module('ds.lab').constant('config', {
  'apiHost': 'http://localhost',
  'docsHost': 'https://seahorse.deepsense.io',
  'notebookHost': 'http://localhost:8888',
  'apiPort': '9080',
  'apiVersion': '1.0.0',
  'editorVersion': '1.0.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://localhost:15674/',
  'socketReconnectionInterval': 1000
});
