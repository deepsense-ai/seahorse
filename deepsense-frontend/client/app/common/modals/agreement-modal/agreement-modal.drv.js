'use strict';

/* @ngInject */
function AgreementModal() {
  return {
    restrict: 'E',
    templateUrl: 'app/common/modals/agreement-modal/agreement-modal.html',
    replace: true,
    controller: 'AgreementModalCtrl',
    controllerAs: 'ctrl',
    bindToController: true
  };
}
exports.function = AgreementModal;

exports.inject = function(module) {
  module.directive('agreementModal', AgreementModal);
};
