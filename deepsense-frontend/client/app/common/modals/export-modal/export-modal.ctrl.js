'use strict';

/* @ngInject */
function ExportModalController($uibModalInstance, $stateParams, config, version, WorkflowsApiClient) {

  const vm = this;

  vm.downloadLink = WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id);

  vm.close = close;
  vm.getExecutorLink = getExecutorLink;
  vm.getDocsHost = getDocsHost;
  vm.getDocsVersion = getDocsVersion;

  function close() {
    $uibModalInstance.dismiss();
  }

  function getExecutorLink() {
    return `https://s3.amazonaws.com/workflowexecutor/releases/${config.apiVersion}/workflowexecutor_2.10-${config.apiVersion}.jar`;
  }

  function getDocsHost() {
    return config.docsHost;
  }

  function getDocsVersion() {
    return version.getDocsVersion();
  }
}

exports.inject = function(module) {
  module.controller('ExportModalController', ExportModalController);
};
