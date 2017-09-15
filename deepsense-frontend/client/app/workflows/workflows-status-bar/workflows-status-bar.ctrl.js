'use strict';

/* @ngInject */
function WorkflowStatusBarController($scope, WorkflowStatusBarService) {

  $scope.getMenuItems = (workflowType, workflowStatus) => WorkflowStatusBarService.getMenuItems(workflowType, workflowStatus);

}

exports.inject = function(module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
