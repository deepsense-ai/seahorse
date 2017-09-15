'use strict';

/* @ngInject */
function ExportModalController(config, version, $uibModalInstance, $stateParams, WorkflowsApiClient) {
  _.assign(this, {
    errorMessage: '',
    warningMessage: '',
    loading: false,
    close: () => {
      $uibModalInstance.dismiss();
    },
    getExecutorLink: () => 'https://s3.amazonaws.com/workflowexecutor/releases/' + config.apiVersion + '/workflowexecutor_2.10-' + config.apiVersion + '.jar',
    getDocsHost: () => config.docsHost,
    getDocsVersion: () => version.getDocsVersion(),
    download: () => {
      $('body')
        .append(angular.element(`
          <iframe style="display: none" src="${WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id)}"></iframe>
        `));
      this.close();
    }
  });
}

exports.inject = function(module) {
  module.controller('ExportModalController', ExportModalController);
};
