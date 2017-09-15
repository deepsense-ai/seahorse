'use strict';

/* @ngInject */
function ExportModalController($modalInstance, $stateParams, WorkflowsApiClient) {
  _.assign(this, {
    errorMessage: '',
    loading: false,
    close: () => {
      $modalInstance.dismiss();
    },
    download: () => {
      this.loading = true;

      let $el = $('#download-iframe');
      $el.load(() => {
        console.log('kozik');
      });
      $el.attr('src', WorkflowsApiClient.downloadWorkflow($stateParams.id));
    }
  });
}

exports.inject = function (module) {
  module.controller('ExportModalController', ExportModalController);
};
