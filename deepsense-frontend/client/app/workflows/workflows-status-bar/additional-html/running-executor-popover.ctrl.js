'use strict';

/* @ngInject */
class RunningExecutorPopoverCtrl {
  constructor() {
    this.isRunningExecutorPopoverVisible = true
  }

  isPopoverVisible() {
    return this.isRunningExecutorPopoverVisible;
  }

  closePopover() {
    this.isRunningExecutorPopoverVisible = false;
  }

}

exports.inject = function (module) {
  module.controller('RunningExecutorPopoverCtrl', RunningExecutorPopoverCtrl);
};
