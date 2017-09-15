'use strict';

/* @ngInject */
function WorkflowStatusBarController($scope, WorkflowStatusBarService) {

  $scope.getMenuItems = (workflow) => WorkflowStatusBarService.getMenuItems(workflow);

}

exports.inject = function(module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
