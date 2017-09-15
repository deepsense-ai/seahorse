'use strict';

/* @ngInject */
function WorkflowCloneModalCtrl($uibModalInstance, originalWorkflow) {

  this.originalWorkflow = originalWorkflow;
  this.workflowCopy = angular.copy(originalWorkflow);
  this.workflowCopy.name = 'Copy of: "' + this.workflowCopy.name + '"';

  _.assign(this, {
    save: () => {
      $uibModalInstance.close(this.workflowCopy);
    },
    dismiss: () => {
      $uibModalInstance.dismiss();
    }
  });
}

exports.inject = function (module) {
  module.controller('WorkflowCloneModalCtrl', WorkflowCloneModalCtrl);
};
