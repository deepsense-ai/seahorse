'use strict';

/* @ngInject */
function WorkflowCloneModalCtrl($uibModalInstance, workflow) {
  this.oldName = workflow.name;
  workflow.name = 'Copy of: "' + workflow.name + '"';

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
