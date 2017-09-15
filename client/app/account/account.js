/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Account($rootScope, PageService) {
  PageService.setTitle('My account');

  $rootScope.stateData.dataIsLoaded = true;

  this.label = 'Account info / settings page';
}
exports.function = Account;

exports.inject = function (module) {
  module.controller('Account', Account);
};
