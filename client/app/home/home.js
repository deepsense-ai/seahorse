/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Home($rootScope, $scope, $state, PageService) {
  PageService.setTitle('Home');

  // Index page should change itself
  $scope.$state = $state;

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
