/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function HomeConfig($stateProvider) {
  $stateProvider.state('home', {
      url: '/',
      templateUrl: 'app/home/home.html',
      controller: 'Home as home'
  });
}

exports.function = HomeConfig;

exports.inject = function (module) {
  module.config(HomeConfig);
};
