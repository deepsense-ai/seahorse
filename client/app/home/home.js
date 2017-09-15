/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Home($rootScope, $scope, PageService) {
  PageService.setTitle('Home');

  $rootScope.stateData.dataIsLoaded = true;

  $scope.uploadWorkflow = function uploadWorkflow (event) {
    event.preventDefault();
  };

  $scope.uploadExecutionReport = function uploadExecutionReport (event) {
    event.preventDefault();
  };
}

exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
