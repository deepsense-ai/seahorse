/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Home() {
  let message = 'Hello!';
  this.welcomeMessage = message;
}
exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
