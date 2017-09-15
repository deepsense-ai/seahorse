/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentController($stateParams, $rootScope, Operations, DrawingService, ExperimentFactory, ExperimentAPIClient, UPDATE_CLICKED_NODE) {

  var that = this;
  var operations;
  var experiment;
  var selectedNodeId;

  that.init = function () {
    Operations.getCatalog().then((data) => {
      that.operationsCatalog = data;
    });

    Operations.getAll()
      .then(function (data) {
        operations = data;
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

  that.getSelectedNode = function getSelectedNode() {
    var experiment = that.getExperiment();
    if (experiment) {
      return experiment.getNodeById(selectedNodeId);
    }
  };

  that.showOperationAttributesPanel = { value: false };

  $rootScope.$on(UPDATE_CLICKED_NODE, function(event, data) {
    if (!that.showOperationAttributesPanel.value) {
      that.showOperationAttributesPanel.value = true;
    } else if (selectedNodeId && selectedNodeId === data.selectedNodeId) {
      // another click at the same graph node closes up the right side panel
      that.showOperationAttributesPanel.value = false;
    }
    selectedNodeId = data.selectedNodeId;
    $rootScope.$apply();
  });

  that.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
  module.constant('UPDATE_CLICKED_NODE', 'update-clicked-node');
};
