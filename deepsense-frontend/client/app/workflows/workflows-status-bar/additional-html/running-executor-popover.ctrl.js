'use strict';

/* @ngInject */
class RunningExecutorPopoverCtrl {
  constructor(WorkflowStatusBarService) {
    this.WorkflowStatusBarService = WorkflowStatusBarService;
  }

  isPopoverVisible() {
    this.WorkflowStatusBarService.isRunningExecutorPopoverVisible();
  }

  closePopover() {
    this.WorkflowStatusBarService.closeRunningExecutorPopover();
  }

}

exports.inject = function (module) {
  module.controller('RunningExecutorPopoverCtrl', RunningExecutorPopoverCtrl);
};
