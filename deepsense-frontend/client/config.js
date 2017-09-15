'use strict';

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

angular.module('ds.lab').factory('version', ['config', function (config) {
  let service = {};
  service.getDocsVersion = function() {
    return config.apiVersion.split('.').slice(0, 2).join('.');
  };
  return service;
}]);
