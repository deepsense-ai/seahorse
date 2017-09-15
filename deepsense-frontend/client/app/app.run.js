'use strict';

/* @ngInject */
function AppRun($rootScope, $uibModalStack) {

  $rootScope.stateData = {
    showView: undefined,
    dataIsLoaded: undefined
  };

  $rootScope.$on('$stateChangeStart', () => {
    $rootScope.stateData.dataIsLoaded = undefined;
    $rootScope.showView = undefined;

    while ($uibModalStack.getTop()) {
      $uibModalStack.dismiss($uibModalStack.getTop().key);
    }
  });

  $rootScope.$on('$stateChangeSuccess', () => {
    $rootScope.stateData.showView = true;
  });
}

exports.function = AppRun;

exports.inject = function(module) {
  module.run(AppRun);
};
