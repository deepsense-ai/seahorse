'use strict';

/* @ngInject */
function SessionManager($interval, config, SessionManagerApi, SessionStatus) {

  const service = {
    isReady: false,
    sessions: [],
    statusForWorkflowId: (workflowId) => {
      const session = _.find(service.sessions, (s) => s.workflowId === workflowId);
      if(_.isUndefined(session)) {
        return SessionStatus.NOT_RUNNING;
      } else {
        return session.status;
      }
    },
    clusterInfoForWorkflowId: (workflowId) => {
      const session = _.find(service.sessions, (s) => s.workflowId === workflowId);
      return session.cluster;
    }
  };

  function pollSessionManager() {
    SessionManagerApi.downloadSessions().
    then((result) => {
      service.sessions = result;
      service.isReady = true;
    }).
    catch(() => {
      service.isReady = false;
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
