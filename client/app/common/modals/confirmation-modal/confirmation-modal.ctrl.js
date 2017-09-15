'use strict';

/* @ngInject */
function ConfirmationModalController($modalInstance, message) {
  _.assign(this, {
    message: message,
    close: () => {
      $modalInstance.dismiss();
    },
    ok: () => {
      $modalInstance.close();
    }
  });
}

exports.inject = function (module) {
  module.controller('ConfirmationModalController', ConfirmationModalController);
};
