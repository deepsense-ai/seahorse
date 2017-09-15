/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Home($rootScope, PageService) {
  PageService.setTitle('Home');

  $rootScope.stateData.dataIsLoaded = true;
}

exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
