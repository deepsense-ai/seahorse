/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Account($rootScope) {
  this.label = 'Account info / settings page';
  $rootScope.headerTitle = 'My account';
}
exports.function = Account;

exports.inject = function (module) {
  module.controller('Account', Account);
};
