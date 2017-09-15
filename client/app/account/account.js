/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Account() {
  this.label = 'Account info / settings page';
}
exports.function = Account;

exports.inject = function (module) {
  module.controller('Account', Account);
};
