'use strict';

/* @ngInject */
function ExportModalController($modalInstance, $stateParams, WorkflowsApiClient, WorkflowService) {
  _.assign(this, {
    errorMessage: '',
    loading: true,
    close: () => {
      $modalInstance.dismiss();
    },
    download: () => {
      $('body').append(angular.element(`
        <iframe style="display: none" src="${WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id)}"></iframe>
      `));
    }
  });

  WorkflowService.saveWorkflow().
    catch(() => {
      this.errorMessage = 'Could not save the workflow';
    }).
    finally(() => {
      this.loading = false;
    });
}

exports.inject = function (module) {
  module.controller('ExportModalController', ExportModalController);
};
