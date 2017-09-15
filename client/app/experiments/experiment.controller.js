/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentController($stateParams, $rootScope, OperationsAPIClient, DrawingService, ExperimentFactory, ExperimentAPIClient) {

  var that = this;
  var operations;
  var experiment;

  that.init = function () {
    OperationsAPIClient.getAll()
      .then(function (data) {
        operations = data.operations;
      })
      .then(function () {
        ExperimentAPIClient.getData($stateParams.id).then(function (data) {
          $rootScope.headerTitle = 'Experiment: ' + data.experiment.name;
          experiment = ExperimentFactory.createExperiment(data, operations);
          DrawingService.renderExperiment(experiment);
        });
      });
  };

  that.onRenderFinish = function onRenderFinish() {
    DrawingService.renderPorts();
    DrawingService.renderConnections();
  };

  that.getExperiment = function getExperiment() {
    return experiment;
  };

  that.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
