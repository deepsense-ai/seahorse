'use strict';

/* @ngInject */
function RunningExecutorPopoverCtrl($rootScope) {
  const vm = this;

  vm.isRunningExecutorPopoverVisible = true;

  vm.isPopoverVisible = isPopoverVisible;
  vm.abort = abort;

  function isPopoverVisible() {
    return vm.isRunningExecutorPopoverVisible;
  }

  function abort() {
    $rootScope.$emit('StatusBar.STOP_EDITING', true);
    vm.isRunningExecutorPopoverVisible = false;
  }
}

exports.inject = function (module) {
  module.controller('RunningExecutorPopoverCtrl', RunningExecutorPopoverCtrl);
};
