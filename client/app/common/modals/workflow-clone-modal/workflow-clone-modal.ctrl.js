'use strict';

/* @ngInject */
function WorkflowCloneModalCtrl($uibModalInstance, originalWorkflow) {

  const vm = this;

  vm.originalWorkflow = originalWorkflow;
  vm.workflowCopy = angular.copy(originalWorkflow);
  vm.workflowCopy.name = `Copy of ${vm.workflowCopy.name}`;

  vm.save = save;
  vm.dismiss = dismiss;

  function save() {
    $uibModalInstance.close(vm.workflowCopy);
  }

  function dismiss() {
    $uibModalInstance.dismiss();
  }
}

exports.inject = function (module) {
  module.controller('WorkflowCloneModalCtrl', WorkflowCloneModalCtrl);
};
