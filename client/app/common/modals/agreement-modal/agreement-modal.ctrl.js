'use strict';

/* @ngInject */
function AgreementModalCtrl($cookies, version) {
  let ctrl = this;

  const COOKIE_NAME = version.editorVersion + '_USER_ACCEPTED_END_USER_AGREEMENT';

  ctrl.userAcceptedAgreement = $cookies.get(COOKIE_NAME) === 'true';
  ctrl.acceptAgreement = () => {
    $cookies.put(COOKIE_NAME, 'true');
    ctrl.userAcceptedAgreement = true;
  };
}

exports.inject = function(module) {
  module.controller('AgreementModalCtrl', AgreementModalCtrl);
};
