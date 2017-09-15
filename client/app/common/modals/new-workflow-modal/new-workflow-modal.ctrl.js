'use strict';

/* @ngInject */
function NewWorkflowModalController($uibModalInstance, WorkflowsApiClient) {
  _.assign(this, {
    name: '',
    description: '',
    loading: false,
    close: () => {
      $uibModalInstance.dismiss();
    },
    ok: () => {
      const DEFAULT_NAME = 'Draft workflow';
      this.loading = true;

      WorkflowsApiClient
        .createWorkflow({
          name: this.name || DEFAULT_NAME,
          description: this.description || ''
        })
        .then((response) => {
          $uibModalInstance.close(response.workflowId);
        })
        .catch(({
          data
        } = {}) => {
          let {
            message
          } = (data || {});
          this.loading = false;
          this.errorMessage = message || 'Server error';
        });
    }
  });
}

exports.inject = function(module) {
  module.controller('NewWorkflowModalController', NewWorkflowModalController);
};
