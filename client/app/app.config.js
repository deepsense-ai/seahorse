/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

let config = require('./../../config.json');

/* @ngInject */
function AppConfig($stateProvider, $urlRouterProvider) {
  // TODO: enable html5mode

  $urlRouterProvider.otherwise('/');

  $stateProvider.state('lab', {
    abstract: true,
    template: '<div ui-view/>'
  });
}

exports.function = AppConfig;

exports.inject = function (module) {
  module.config(AppConfig);

  module.constant('config', {
    'apiHost': config.env.proxy.host,
    'apiPort': config.env.proxy.port
  });
};
