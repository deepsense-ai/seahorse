/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


/* @ngInject */
function ExperimentListController($rootScope, ExperimentAPIClient) {

  ExperimentAPIClient.getList().then((data) => {
    this.experiments = data.experiments;
  });

  this.experiments = {};
  $rootScope.headerTitle = 'My experiments';
}

exports.inject = function (module) {
  module.controller('ExperimentListController', ExperimentListController);
};
