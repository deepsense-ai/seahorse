/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ErrorsConfig($stateProvider) {
  $stateProvider.
    state('lab.errorState', {
      url: '/error',
      views: {
        'errorView@': {
          templateUrl: 'app/errors/error.html',
          /* @ngInject */
          controller: ($stateParams, $rootScope) => {
            $rootScope.stateData.dataIsLoaded = true;
          }
        }
      }
    });
}

exports.function = ErrorsConfig;

exports.inject = function (module) {
  module.config(ErrorsConfig);
};
