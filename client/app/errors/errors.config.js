'use strict';

/* @ngInject */
function ErrorsConfig($stateProvider) {
  $stateProvider.state('MissingState', {
    url: '/error/missing',
    templateUrl: 'app/errors/error-missing.html',
    controller: 'ErrorController as controller'
  });

  $stateProvider.state('ConflictState', {
    url: '/:type/error/version/:id',
    params: {
      id: undefined,
      type: undefined,
      errorMessage: undefined
    },
    templateUrl: 'app/errors/error-version.html',
    controller: 'ErrorController as controller'
  });
}

exports.function = ErrorsConfig;

exports.inject = function(module) {
  module.config(ErrorsConfig);
};
