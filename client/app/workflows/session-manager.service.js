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
