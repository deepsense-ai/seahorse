'use strict';

/* @ngInject */
function ExportModalController($modalInstance, $stateParams, WorkflowsApiClient) {
  _.assign(this, {
    errorMessage: '',
    loading: false,
    close: () => {
      $modalInstance.dismiss();
    },
    download: () => {
      $('body').append(angular.element(`
        <iframe style="display: none" src="${WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id)}"></iframe>
      `));
    }
  });
}

exports.inject = function (module) {
  module.controller('ExportModalController', ExportModalController);
};
