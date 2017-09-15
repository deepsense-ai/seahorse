'use strict';

/* @ngInject */
function SessionManager($interval, SessionManagerApi, SessionStatus) {

  const service = {
    sessions: [],
    statusForWorkflowId: (workflowId) => {
      const session = _.find(service.sessions, (s) => s.workflowId === workflowId);
      if(_.isUndefined(session)) {
        return SessionStatus.NOT_RUNNING
      } else {
        return session.status;
      }
    }
  };

  $interval(() => { // SM polling
    SessionManagerApi.downloadSessions().then((result) => {
      service.sessions = result;
    });
  }, 1000);

  return service;
}

exports.function = SessionManager;

exports.inject = function(module) {
  module.factory('SessionManager', SessionManager);
};
