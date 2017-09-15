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

    _unbindDeepsenseCustomListeners();
    _clearModalsFromStack();
  });

  $rootScope.$on('$stateChangeSuccess', () => {
    $rootScope.stateData.showView = true;
  });

  function _unbindDeepsenseCustomListeners() {
    for(let key in $rootScope.$$listeners) {
      if (key[0] !== '$') {
        delete $rootScope.$$listeners[key];
      }
    }
  }

  function _clearModalsFromStack() {
    while ($uibModalStack.getTop()) {
      $uibModalStack.dismiss($uibModalStack.getTop().key);
    }
  }
}

exports.function = AppRun;

exports.inject = function(module) {
  module.run(AppRun);
};
