'use strict';

/* @ngInject */
function GeneralDataPanelController($modal, $scope, $timeout, WorkflowService) {
  this.saveWorkflow = () => {
    WorkflowService.saveWorkflow();
  };

  this.showErrorMessage = () => {
    $modal.open({
      scope: $scope,
      template: `
        <h2>Error title:</h2>
        <pre class="o-error-trace">{{::controller.state.error.title || 'No title'}}</pre>
        <h2>Error message:</h2>
        <pre class="o-error-trace">{{::controller.state.error.message || 'No message'}}</pre>
      `,
      windowClass: 'o-modal--error'
    });
  };

  this.getVerboseStatus = () => this.state.status.toUpperCase()
    .split('_')[1];

  this.delaySave = function delaySave() {
    $timeout(this.saveWorkflow, 0, false);
  };
}

exports.inject = function(module) {
  module.controller('GeneralDataPanelController', GeneralDataPanelController);
};
