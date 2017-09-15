'use strict';

/* @ngInject */
function AppRun($rootScope) {

  $rootScope.stateData = {
    showView: undefined,
    dataIsLoaded: undefined
  };

  $rootScope.$on('$stateChangeStart', () => {
    $rootScope.stateData.dataIsLoaded = undefined;
    $rootScope.showView = undefined;
  });

  $rootScope.$on('$stateChangeSuccess', () => {
    $rootScope.stateData.showView = true;
  });
}

exports.function = AppRun;

exports.inject = function(module) {
  module.run(AppRun);
};
