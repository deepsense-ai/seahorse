'use strict';

/* @ngInject */
function ExportModalController($modalInstance, $stateParams, WorkflowsApiClient, WorkflowService) {
  _.assign(this, {
    errorMessage: '',
    warningMessage: '',
    loading: true,
    close: () => {
      $modalInstance.dismiss();
    },
    download: () => {
      $('body')
        .append(angular.element(`
        <iframe style="display: none" src="${WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id)}"></iframe>
      `));
    }
  });

  WorkflowService.saveWorkflow()
    .
  catch(() => {
      this.errorMessage = 'Could not save the workflow';
    })
    .
  then(() => {
    this.loading = false;

    let workflow = WorkflowService.getWorkflow();
    let nodes = workflow.getNodes();
    let errorsExist = _.any(_.map(nodes, node => node.knowledgeErrors && node.knowledgeErrors.length > 0));

    if (errorsExist) {
      this.warningMessage = `You are trying to export a workflow which still contains at least one flawed node.
          Check if there is any node with the exclamation mark icon on it in order to see all errors related to the node.`;
    }
  });
}

exports.inject = function(module) {
  module.controller('ExportModalController', ExportModalController);
};
