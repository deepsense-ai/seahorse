'use strict';

/* @ngInject */
function GeneralDataPanelController ($modal, $scope, WorkflowService) {
  this.saveWorkflow = () => {
    WorkflowService.saveWorkflow();
  };

  this.showErrorMessage = () => {
    $modal.open({
      scope: $scope,
      template: `
          <h2>Error message:</h2>
          <pre class="o-error-trace">{{::this.additionalData.error.message}}</pre>`,
      windowClass: 'o-modal--error'
    });
  };
}

exports.inject = function (module) {
  module.controller('GeneralDataPanelController', GeneralDataPanelController);
};
