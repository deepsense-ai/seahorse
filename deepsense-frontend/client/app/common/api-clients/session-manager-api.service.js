'use strict';

/* @ngInject */
function SessionManagerApi($http, config) {

  const service = this;

  service.downloadSessions = downloadSessions;
  service.downloadSessionById = downloadSessionById;
  service.deleteSessionById = deleteSessionById;
  service.startSession = startSession;

  const URL = `${config.apiHost}:${config.sessionApiPort}/v1/sessions`;

  function downloadSessions() {
    return $http.get(URL).then(function processResult(result) {
      console.log('SessionManagerApi downloadSessions result', result);
      return result.data.sessions;
    }, function handleError(error) {
      console.log('error', error);
    });
  }

  function downloadSessionById(sessionId) {
    return $http.get(`${URL}/${sessionId}`).then(function processResult(result) {
      console.log('SessionManagerApi downloadSessionById result', result);
      return result.data;
    }, function handleError(error) {
      console.log('error', error);
    });
  }

  function deleteSessionById(sessionId) {
    return $http.delete(`${URL}/${sessionId}`).then(function processResult(result) {
      return result;
    }, function handleError(error) {
      console.log('error', error);
    });
  }

  function startSession(workflowId) {
    return $http.post(URL, {workflowId: workflowId}).then(function processResult(result) {
      return result;
    }, function handleError(error) {
      console.log('error', error);
    });
  }

  return service;

}

exports.inject = function(module) {
  module.service('SessionManagerApi', SessionManagerApi);
};
