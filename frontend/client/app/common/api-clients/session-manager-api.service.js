/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

/* @ngInject */
function SessionManagerApi($http, config, $log) {

  const service = this;

  service.downloadSessions = downloadSessions;
  service.downloadSessionByWorkflowId = downloadSessionByWorkflowId;
  service.deleteSessionById = deleteSessionById;
  service.startSession = startSession;

  const URL = config.sessionApiPort ?
    `${config.apiHost}:${config.sessionApiPort}/${config.urlApiVersion}/sessions` :
    `${config.apiHost}/${config.urlApiVersion}/sessions`;

  function downloadSessions(config) {
    return $http.get(URL, config).then(function processResult(result) {
      $log.log('SessionManagerApi downloadSessions result', result);
      return result.data.sessions;
    });
  }

  function downloadSessionByWorkflowId(workflowId) {
    return $http.get(`${URL}/${workflowId}`).then(function processResult(result) {
      $log.log('SessionManagerApi downloadSessionById result', result);
      return result.data;
    });
  }

  function deleteSessionById(workflowId) {
    return $http.delete(`${URL}/${workflowId}`).then(function processResult(result) {
      return result;
    });
  }

  function startSession(config) {
    return $http.post(URL, config).then(function processResult(result) {
      return result;
    });
  }

  return service;

}

exports.inject = function(module) {
  module.service('SessionManagerApi', SessionManagerApi);
};
