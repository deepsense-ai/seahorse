'use strict';

/* @ngInject */
function UploadWorkflowModalController($modalInstance) {
  _.assign(this, {
    loading: false,
    close: () => {
      $modalInstance.dismiss();
    },
    ok: () => {
      this.loading = true;
      $modalInstance.close();
    }
  });
}

exports.inject = function (module) {
  module.controller('UploadWorkflowModalController', UploadWorkflowModalController);
};
