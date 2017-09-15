'use strict';

/* @ngInject */
function UploadWorkflowExecutionReportModalController($modalInstance, Upload) {

  const STATUS_PREPARING = 'preparing';
  const STATUS_LOADING = 'loading';
  const STATUS_SUCCESS = 'success';
  const STATUS_FAILURE = 'failure';

  _.assign(this, {
    name: '',
    description: '',
    status: STATUS_PREPARING,
    errorMessage: '',
    progress: '',
    close: () => {
      $modalInstance.dismiss();
    },
    upload: function (file) {
      this.status = STATUS_FAILURE;
      Upload.upload({
        url: 'upload/url',
        file: file
      }).progress((evt) => {
        this.status = STATUS_LOADING;
        this.progress = parseInt(100.0 * evt.loaded / evt.total);
      }).then(() => {
        this.status = STATUS_SUCCESS;
      }).catch((error = {data: 'Server is not responding'}) => {
        this.status = STATUS_FAILURE;
        this.errorMessage = error.data;
      });
    },
    ok: function () {
      $modalInstance.close();
    }
  });
}

exports.inject = function (module) {
  module.controller('UploadWorkflowExecutionReportModalController', UploadWorkflowExecutionReportModalController);
};
