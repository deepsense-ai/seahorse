'use strict';

/* @ngInject */
function NewWorkflowModalController($modalInstance, WorkflowsApiClient) {
  _.assign(this, {
    name: '',
    description: '',
    loading: false,
    close: () => {
      $modalInstance.dismiss();
    },
    ok: () => {
      const DEFAULT_NAME = 'Draft workflow';
      this.loading = true;

      WorkflowsApiClient.
        createWorkflow({
          name: this.name || DEFAULT_NAME,
          description: this.description || ''
        }).
        then((response) => {
          $modalInstance.close(response.id);
        }).
        catch(({ data } = {}) => {
          let { message } = data;
          this.loading = false;
          this.errorMessage = message || 'Server error';
        });
    }
  });
}

exports.inject = function (module) {
  module.controller('NewWorkflowModalController', NewWorkflowModalController);
};
