'use strict';

/* @ngInject */
function WorkflowStatusBarController($scope, WorkflowStatusBarService) {

  $scope.getMenuItems = (workflowType, isRunning) => WorkflowStatusBarService.getMenuItems(workflowType, isRunning);

}

exports.inject = function(module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
