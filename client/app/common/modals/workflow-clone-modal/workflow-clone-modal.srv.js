'use strict';

import tpl from './workflow-clone-modal.html';

/* @ngInject */
function WorkflowCloneService($uibModal, WorkflowsApiClient, NotificationService) {

  this.openModal = (callback, workflow) => {
    const modal = $uibModal.open({
      animation: true,
      templateUrl: tpl,
      controller: 'WorkflowCloneModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        originalWorkflow: () => workflow
      }
    });

    modal.result.then((workflowToClone) => {
      WorkflowsApiClient.cloneWorkflow(workflowToClone).then(callback, () => {
        NotificationService.showWithParams({
          message: 'There was an error during copying workflow.',
          title: 'Workflow copy',
          settings: {timeOut: 10000},
          notificationType: 'error'
        });
      });
    });
  };
}

exports.inject = function (module) {
  module.service('WorkflowCloneService', WorkflowCloneService);
};
