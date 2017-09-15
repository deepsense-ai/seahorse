'use strict';

/* @ngInject */
function ErrorsConfig($stateProvider) {
  $stateProvider.
  state('errorState', {
    url: '/error',
    views: {
      '@': {
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

exports.inject = function(module) {
  module.config(ErrorsConfig);
};
