'use strict';

/* @ngInject */
class RunningExecutorPopoverCtrl {
  constructor(WorkflowStatusBarService) {
    this.WorkflowStatusBarService = WorkflowStatusBarService;
  }

  isPopoverVisible() {
    this.WorkflowStatusBarService.userClosedRunningExecutorPopover();
  }

  closePopover() {
    this.WorkflowStatusBarService.closeStartingExecutorPopover();
  }

}

exports.inject = function (module) {
  module.controller('RunningExecutorPopoverCtrl', RunningExecutorPopoverCtrl);
};
