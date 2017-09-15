'use strict';

/* @ngInject */
class ExecutorErrorCtrl {
  constructor(WorkflowService, SessionManagerApi) {
    this.visible = true;
    this.WorkflowService = WorkflowService;
    this.SessionManagerApi = SessionManagerApi;
    this.workflow = this.WorkflowService.getCurrentWorkflow();
    this.currentTime = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
  }

  isPopoverVisible() {
    return this.visible;
  }

  runExecutorAgain() {
    this.visible = false;
    this.SessionManagerApi.deleteSessionById(this.workflow.id)
      .then(() => {
        return this.SessionManagerApi.startSession(this.workflow.id);
      });
  }

}

exports.inject = function (module) {
  module.controller('ExecutorErrorCtrl', ExecutorErrorCtrl);
};
