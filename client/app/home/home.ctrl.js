'use strict';

/* @ngInject */
function Home($rootScope, $uibModal, $state, WorkflowService, PageService, config) {

  this.init = () => {
    PageService.setTitle('Home');
    this.$state = $state;
    $rootScope.stateData.dataIsLoaded = true;
  };

  this.getVersion = () => {
    return config.editorVersion;
  };

  this.getAllWorkflows = () => {
    return WorkflowService.getAllWorkflows();
  };

  this.getWorkflowUrl = (workflowId) => {
    return '/#/workflows/' + workflowId + '/editor';
  };

  this.displayCreateWorkflowPopup = (event) => {
    event.preventDefault();

    let modal = $uibModal.open({
      animation: true,
      templateUrl: 'app/common/modals/new-workflow-modal/new-workflow-modal.html',
      controller: 'NewWorkflowModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.then((workflowId) => {
      $state.go('workflows.editor', {
        id: workflowId
      });
    });
  };

  this.displayUploadWorkflowPopup = (event) => {
    event.preventDefault();

    let modal = $uibModal.open({
      animation: true,
      templateUrl: 'app/common/modals/upload-workflow-modal/upload-workflow-modal.html',
      controller: 'UploadWorkflowModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.then((workflowId) => {
      $state.go('workflows.editor', {
        'id': workflowId
      });
    });
  };

  this.displayUploadExecutionWorkflowPopup = (event) => {
    event.preventDefault();

    let modal = $uibModal.open({
      animation: false,
      templateUrl: 'app/common/modals/upload-execution-report-modal/upload-execution-report-modal.html',
      controller: 'UploadWorkflowExecutionReportModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.then((reportId) => {
      $state.go('workflows.report', {
        'reportId': reportId
      });
    });
  };

  this.init();
}

exports.function = Home;

exports.inject = function(module) {
  module.controller('Home', Home);
};
