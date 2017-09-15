'use strict';

// THIS FILE IS TEMPLATED IN BUILD PROCESS
// CHANGES HERE WILL BE LOST IN BUILD IF NOT PROPAGATED TO BUILD PIPELINE.

angular.module('ds.lab').constant('config', {
  // localhost - from docker compose
  'apiHost': window.location.protocol + '//' + window.location.hostname,
  'docsHost': 'https://seahorse.deepsense.io',
  'notebookHost': window.location.protocol + '//' + window.location.host + '/jupyter',
  'sessionApiPort': window.location.port,
  'sessionPollingInterval': 1000,
  'apiPort': window.location.port,
  'apiVersion': '1.2.0',
  'editorVersion': '1.2.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': window.location.protocol + '//' + window.location.host + '/',
  'socketReconnectionInterval': 1000,
  'mqUser': 'yNNp7VJS',
  'mqPass': '1ElYfGNW'
});
