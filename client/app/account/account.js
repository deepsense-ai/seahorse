/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Account(PageService) {
  this.label = 'Account info / settings page';
  PageService.setTitle('My account');
}
exports.function = Account;

exports.inject = function (module) {
  module.controller('Account', Account);
};
