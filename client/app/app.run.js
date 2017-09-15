/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

/* @ngInject */
function AppRun($rootScope, $state, PageService) {
  $rootScope.stateData = {
    showView: undefined,
    errorMessage: undefined,
    dataIsLoaded: undefined
  };

  $rootScope.transitToErrorState = (errorMessage) => {
    $rootScope.stateData.errorMessage = errorMessage;
    $state.go('errorState');
  };

  $rootScope.$on('$stateChangeStart', (event, toState) => {
    _.assign($rootScope.stateData, {
      dataIsLoaded: undefined,
      showView: undefined
    });

    // keep the old value while redirection to the error view
    if (toState.name !== 'errorState') {
      $rootScope.stateData.errorMessage = undefined;
    }

    PageService.setTitle('');
  });

  $rootScope.$on('$stateChangeSuccess', () => {
    $rootScope.stateData.showView = true;
  });

  $rootScope.$watch('stateData.errorMessage', (newErrorMessage) => {
    if (newErrorMessage) {
      $state.go('errorState');
    }
  });
}

exports.function = AppRun;

exports.inject = function (module) {
  module.run(AppRun);
};
