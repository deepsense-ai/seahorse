'use strict';

/* @ngInject */
function WorkflowCloneModalCtrl($uibModalInstance, workflow) {
  workflow.name = workflow.name + ' - Clone';

  _.assign(this, {
    workflow: workflow,
    save: () => {
      $uibModalInstance.close(this.workflow);
    },
    dismiss: () => {
      $uibModalInstance.dismiss();
    }
  });
}

exports.inject = function (module) {
  module.controller('WorkflowCloneModalCtrl', WorkflowCloneModalCtrl);
};
