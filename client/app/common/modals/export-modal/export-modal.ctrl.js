'use strict';

/* @ngInject */
function ExportModalController($uibModalInstance, $stateParams, WorkflowsApiClient) {

  const vm = this;

  vm.close = close;
  vm.download = download;

  function download() {
    /**
     * Iframe is needed because Firefox on download link click closes all WebSocket connection.
     * By using iframe, we remove the bug of "Reconnecting" message appearing while exporting workflow
     */
    $('body')
      .append(angular.element(`
          <iframe style="display: none" src="${WorkflowsApiClient.getDownloadWorkflowMethodUrl($stateParams.id)}"></iframe>
        `));
    close();
  }

  function close() {
    $uibModalInstance.dismiss();
  }
}

exports.inject = function(module) {
  module.controller('ExportModalController', ExportModalController);
};
