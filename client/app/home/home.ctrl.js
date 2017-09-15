/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Home($rootScope, $modal, $scope, $state, PageService) {
  PageService.setTitle('Home');

  // Index page should change itself
  $scope.$state = $state;

  $rootScope.stateData.dataIsLoaded = true;

  $scope.displayPopup = function displayPopup(event) {
    event.preventDefault();
    let modal =  $modal.open({
      animation: true,
      templateUrl: 'app/common/modals/new-workflow-modal/new-workflow-modal.html',
      controller: 'NewWorkflowModalController as newWorkflowModalController',
      backdrop: 'static',
      keyboard: false
    });

    modal.result.
      then((workflowId) => {
        $state.go('workflows_editor', {
          id: workflowId
        });
      });
  };

  $scope.uploadWorkflow = function uploadWorkflow (event) {
    event.preventDefault();
  };

  $scope.uploadExecutionReport = function uploadExecutionReport (event) {
    event.preventDefault();
  };
}

exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
