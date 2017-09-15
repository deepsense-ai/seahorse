'use strict';

let NewWorkflowModalController = /* @ngInject */ function NewWorkflowModalController($scope, $modalInstance, WorkflowsApiClient) {
  _.assign(this, {
    name: '',
    description: '',
    loading: false,
    close: () => {
      $modalInstance.dismiss();
    },
    ok: () => {
      this.loading = true;

      WorkflowsApiClient.
        createWorkflow({
          name: this.name || 'Draft workflow',
          description: this.description
        }).
        then((response) => {
          $modalInstance.close(response.id);
        }).
        catch((reason) => {
          this.loading = false;
          this.errorMessage = reason;
        });
    }
  });
};

exports.inject = function (module) {
  module.controller('NewWorkflowModalController', NewWorkflowModalController);
};
