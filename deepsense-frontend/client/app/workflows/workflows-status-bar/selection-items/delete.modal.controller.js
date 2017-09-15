'use strict';

/* @ngInject */
function DeleteConfirmationModalController($uibModalInstance) {
  const vm = this;
  vm.ok = ok;
  vm.close = close;
  vm.doNotShowAgain = false;

  return vm;

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
