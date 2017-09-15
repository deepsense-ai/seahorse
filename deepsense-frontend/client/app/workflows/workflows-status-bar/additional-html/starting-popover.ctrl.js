'use strict';

/* @ngInject */
class StartingPopoverCtrl {
  constructor(WorkflowStatusBarService) {
    this.WorkflowStatusBarService = WorkflowStatusBarService;
  }

  isPopoverVisible() {
    return this.WorkflowStatusBarService.isStartingPopoverVisible();
  }

  closePopover() {
    this.WorkflowStatusBarService.closeStartingPopover();
  }

}

exports.inject = function (module) {
  module.controller('StartingPopoverCtrl', StartingPopoverCtrl);
};
