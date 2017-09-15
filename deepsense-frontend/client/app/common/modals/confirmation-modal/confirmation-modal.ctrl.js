'use strict';

/* @ngInject */
function ConfirmationModalController($uibModalInstance, message) {
  _.assign(this, {
    message: message,
    close: () => {
      $uibModalInstance.dismiss();
    },
    ok: () => {
      $uibModalInstance.close();
    }
  });
}

exports.inject = function(module) {
  module.controller('ConfirmationModalController', ConfirmationModalController);
};
