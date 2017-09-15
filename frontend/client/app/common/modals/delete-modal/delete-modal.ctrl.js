'use strict';

/* @ngInject */
function DeleteConfirmationModalController($uibModalInstance) {
  const vm = this;

  vm.doNotShowAgain = false;

  vm.ok = ok;
  vm.close = close;

  function close() {
    $uibModalInstance.dismiss();
  }

  function ok() {
    $uibModalInstance.close(vm.doNotShowAgain);
  }
}

exports.inject = function (module) {
  module.controller('DeleteConfirmationModalController', DeleteConfirmationModalController);
};
