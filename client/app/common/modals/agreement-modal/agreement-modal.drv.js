'use strict';

import tpl from './agreement-modal.html';

/* @ngInject */
function AgreementModal() {
  return {
    restrict: 'E',
    templateUrl: tpl,
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
