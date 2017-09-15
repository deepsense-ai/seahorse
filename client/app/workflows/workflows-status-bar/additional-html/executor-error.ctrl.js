'use strict';

import moment from 'moment';

/* @ngInject */
class ExecutorErrorCtrl {
  constructor($rootScope, WorkflowService, SessionManagerApi) {
    this.visible = true;
    this.WorkflowService = WorkflowService;
    this.SessionManagerApi = SessionManagerApi;
    this.$rootScope = $rootScope;
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
        return this.$rootScope.$broadcast('StatusBar.START_EDITING');
      });
  }

}

exports.inject = function (module) {
  module.controller('ExecutorErrorCtrl', ExecutorErrorCtrl);
};
