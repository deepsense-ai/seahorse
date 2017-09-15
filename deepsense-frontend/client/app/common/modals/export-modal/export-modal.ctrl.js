'use strict';

/* @ngInject */
function ExportModalController($uibModalInstance, $stateParams, WorkflowsApiClient) {

  const vm = this;

  vm.downloadLink = WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id);

  vm.close = close;

  function close() {
    $uibModalInstance.dismiss();
  }
}

exports.inject = function(module) {
  module.controller('ExportModalController', ExportModalController);
};
