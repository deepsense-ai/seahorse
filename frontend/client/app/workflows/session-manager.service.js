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

import {sessionStatus} from 'APP/enums/session-status.js';

const CHECKING_SESSION_MANAGER_STATE_TIMEOUT = 10000;
/* @ngInject */
function SessionManager($interval, config, SessionManagerApi) {

  const service = {
    sessions: [],
    statusForWorkflowId: (workflowId) => {
      const session = _.find(service.sessions, (s) => s.workflowId === workflowId);
      if(_.isUndefined(session)) {
        return sessionStatus.NOT_RUNNING;
      } else {
        return session.status;
      }
    },
    clusterInfoForWorkflowId: (workflowId) => {
      const session = _.find(service.sessions, (s) => s.workflowId === workflowId);
      return session.cluster;
    },
    checkSessionManagerState: () => {
      return SessionManagerApi.downloadSessions({timeout: CHECKING_SESSION_MANAGER_STATE_TIMEOUT});
    }
  };

  function pollSessionManager() {
    SessionManagerApi.downloadSessions()
      .then((result) => {
        service.sessions = result;
      });
  }

  pollSessionManager();
  $interval(() => { // SM polling
    pollSessionManager();
  }, config.sessionPollingInterval);

  return service;
}

exports.function = SessionManager;

exports.inject = function(module) {
  module.factory('SessionManager', SessionManager);
};
