/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var dagDemo = require('./dag-demo.js');

/* @ngInject */
function ExperimentController($stateParams, $rootScope, ExperimentAPIClient) {
  ExperimentAPIClient.getData($stateParams.id).then((data) => {
    $rootScope.headerTitle = 'Experiment: ' + data.experiment.name;
  });

  $rootScope.headerTitle = 'Experiment: ';

  dagDemo();
}
exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
