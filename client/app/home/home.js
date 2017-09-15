/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Home($rootScope) {
  let message = 'Hello! DeepSense.io engine at Your service!';
  this.welcomeMessage = message;
  $rootScope.headerTitle = 'Home';
}
exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
