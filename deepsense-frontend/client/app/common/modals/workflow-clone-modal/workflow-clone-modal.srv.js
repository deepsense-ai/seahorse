'use strict';

/* @ngInject */
function WorkflowCloneService($uibModal, WorkflowsApiClient, NotificationService) {

  this.openModal = (callback, workflow) => {
    const modal = $uibModal.open({
      animation: true,
      templateUrl: 'app/common/modals/workflow-clone-modal/workflow-clone-modal.html',
      controller: 'WorkflowCloneModalCtrl as controller',
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
  }

}

exports.inject = function (module) {
  module.service('WorkflowCloneService', WorkflowCloneService);
};
