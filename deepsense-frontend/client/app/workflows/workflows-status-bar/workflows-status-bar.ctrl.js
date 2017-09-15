'use strict';

/* @ngInject */
function WorkflowStatusBarController($scope, UserService, SessionStatus, WorkflowStatusBarService) {

  $scope.getMenuItems = (workflow) => {
    return WorkflowStatusBarService.getMenuItems(workflow);
  };

  $scope.getCurrentUser = () => {
    return UserService.getSeahorseUser();
  };
  
  $scope.isOwner = (workflow) => {
    return workflow.owner.id === UserService.getSeahorseUser().id;
  };

  $scope.isViewerMode = (workflow) => {
    return workflow.sessionStatus === SessionStatus.NOT_RUNNING;
  };

}

exports.inject = function (module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
