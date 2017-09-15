/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentController($stateParams, $rootScope, Operations, DrawingService, ExperimentFactory, ExperimentAPIClient) {

  var that = this;
  var internal = {};

  var GraphNode = require('./common-objects/common-graph-node.js');

  internal.operations = null;
  internal.experiment = null;
  internal.selectedNode = null;

  internal.init = function init() {
    Operations.getCatalog().then((data) => {
      that.operationsCatalog = data;
  });

  Operations.getAll()
    .then(function (data) {
      internal.operations = data;
    })
    .then(function () {
      ExperimentAPIClient.getData($stateParams.id).then(function (data) {
        $rootScope.headerTitle = 'Experiment: ' + data.experiment.name;
        internal.experiment = ExperimentFactory.createExperiment(data, internal.operations);
        DrawingService.renderExperiment(internal.experiment);
      });
    });
  };

  that.onRenderFinish = function onRenderFinish() {
    DrawingService.renderPorts();
    DrawingService.renderConnections();
  };

  that.getExperiment = function getExperiment() {
    return internal.experiment;
  };

  that.getParametersSchemaById = function getParametersSchemaById(id) {
    return internal.experiment.getParametersSchema()[id];
  };

  that.getSelectedNode = function getSelectedNode() {
    return internal.selectedNode;
  };

  that.unselectNode = function unselectNode() {
    internal.selectedNode = null;
  };

  $rootScope.$on(GraphNode.CLICK, function(event, data) {
    internal.selectedNode = data.selectedNode;
    $rootScope.$apply();
  });

  internal.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
