'use strict';

/* @ngInject */
function Home($rootScope, $modal, $state, PageService) {
  PageService.setTitle('Home');

  // Index page should change itself
  this.$state = $state;

  $rootScope.stateData.dataIsLoaded = true;

  this.displayCreateWorkflowPopup = function displayCreateWorkflowPopup(event) {
    event.preventDefault();

    let modal =  $modal.open({
      animation: true,
      templateUrl: 'app/common/modals/new-workflow-modal/new-workflow-modal.html',
      controller: 'NewWorkflowModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.
      then((workflowId) => {
        $state.go('workflows.editor', {
          id: workflowId
        });
      });
  };

  this.displayUploadWorkflowPopup = function displayUploadWorkflowPopup(event) {
    event.preventDefault();

    let modal = $modal.open({
      animation: true,
      templateUrl: 'app/common/modals/upload-workflow-modal/upload-workflow-modal.html',
      controller: 'UploadWorkflowModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.
      then((workflowId) => {
        $state.go('workflows.editor', {
          'id': workflowId
        });
      });
  };

  this.displayUploadExecutionWorkflowPopup = function displayUploadExecutionWorkflowPopup(event) {
    event.preventDefault();

    let modal = $modal.open({
      animation: false,
      templateUrl: 'app/common/modals/upload-execution-report-modal/upload-execution-report-modal.html',
      controller: 'UploadWorkflowExecutionReportModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.
      then((reportId) => {
        $state.go('workflows.report', {
          'reportId': reportId
        });
      });
  };
}

exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
