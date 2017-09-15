'use strict';

// THIS FILE IS TEMPLATED IN BUILD PROCESS
// CHANGES HERE WILL BE LOST IN BUILD IF NOT PROPAGATED TO BUILD PIPELINE.

angular.module('ds.lab').constant('config', {
  // localhost - from docker compose
  'apiHost': 'http://localhost',
  'docsHost': 'https://seahorse.deepsense.io',
  'notebookHost': 'http://localhost:8080',
  'sessionApiPort': '8080',
  'sessionPollingInterval': 1000,
  'apiPort': '8080',
  'apiVersion': '1.2.0',
  'editorVersion': '1.2.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': 'http://localhost:8080/',
  'socketReconnectionInterval': 1000,
  'mqUser': 'yNNp7VJS',
  'mqPass': '1ElYfGNW'
});
