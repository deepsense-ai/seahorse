'use strict';

import errorMissingTpl from './error-missing.html';
import errorVersionTpl from './error-version.html';
import errorTimeoutTpl from './error-request-timeout.html';

/* @ngInject */
function ErrorsConfig($stateProvider) {
  $stateProvider.state('MissingState', {
    url: '/error/missing',
    templateUrl: errorMissingTpl,
    controller: 'ErrorController as controller'
  });

  $stateProvider.state('ConflictState', {
    url: '/:type/error/version/:id/',
    params: {
      id: undefined,
      type: undefined,
      errorMessage: undefined
    },
    templateUrl: errorVersionTpl,
    controller: 'ErrorController as controller'
  });

  $stateProvider.state('RequestTimeout', {
    url: '/error/request-timeout',
    templateUrl: errorTimeoutTpl,
    controller: 'ErrorController as controller'
  });
}

exports.function = ErrorsConfig;

exports.inject = function(module) {
  module.config(ErrorsConfig);
};
