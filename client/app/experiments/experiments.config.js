/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentsConfig($stateProvider) {
  $stateProvider
    .state('lab.experiments', {
      url: '/experiments',
      templateUrl: 'app/experiments/experiment-list.html',
      controller: 'ExperimentListController as experimentList'
    })
    .state('lab.experiment', {
      url: '/experiment/:id',
      templateUrl: 'app/experiments/experiment.html',
      controller: 'ExperimentController as experiment'
    });
}
exports.function = ExperimentsConfig;

exports.inject = function (module) {
  module.config(ExperimentsConfig);
};
