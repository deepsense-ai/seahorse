/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

/* @ngInject */
function AppRun($rootScope, PageService) {
  $rootScope.$on( '$stateChangeStart', () => {
    $rootScope.stateData = {
      isDataLoaded: undefined,
      errorMessage: undefined,
      showView: undefined
    };

    PageService.setTitle('');
  });

  $rootScope.$on('$stateChangeSuccess', () => {
    $rootScope.stateData.showView = true;
  });
}
exports.function = AppRun;

exports.inject = function (module) {
  module.run(AppRun);
};
