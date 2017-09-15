/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function AccountConfig($stateProvider) {
  $stateProvider.state('lab.account', {
      url: '/account',
      templateUrl: 'app/account/account.html',
      controller: 'Account as account'
  });
}
exports.function = AccountConfig;

exports.inject = function (module) {
  module.config(AccountConfig);
};
