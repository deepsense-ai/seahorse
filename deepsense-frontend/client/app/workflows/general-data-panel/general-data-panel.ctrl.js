'use strict';

/* @ngInject */
function GeneralDataPanelController($uibModal, $scope, $timeout, WorkflowService) {

  this.showErrorMessage = () => {
    $uibModal.open({
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

  this.getVerboseStatus = () => this.state.status.toUpperCase().split('_')[1];

}

exports.inject = function(module) {
  module.controller('GeneralDataPanelController', GeneralDataPanelController);
};
