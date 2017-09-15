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
      let $el = angular.element(`<iframe style="display: none" src="${WorkflowsApiClient.getDownloadWorkflowUrl($stateParams.id)}"></iframe>`);
      $('body').append($el);
    }
  });
}

exports.inject = function (module) {
  module.controller('ExportModalController', ExportModalController);
};
