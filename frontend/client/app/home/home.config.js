'use strict';

import tpl from './home.html';

/* @ngInject */
function HomeConfig($stateProvider) {
  $stateProvider.state('home', {
    url: '/',
    templateUrl: tpl,
    controller: 'Home as home'
  });
}

exports.function = HomeConfig;

exports.inject = function(module) {
  module.config(HomeConfig);
};
