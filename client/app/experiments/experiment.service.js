/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Experiment = require('./common-objects/common-experiment.js');

function ExperimentService() {
  var that = this;
  var internal = {};

  that.createExperiment = function createExperiment(data, operations) {
    var experiment = new Experiment();

    experiment.setData(data.experiment);
    experiment.createNodes(data.experiment.graph.nodes, operations, data.experiment.state);
    experiment.createEdges(data.experiment.graph.edges);
    experiment.updateState(data.experiment.state);

    return experiment;
  };

  that.getExperiment = function getExperiment () {
    return internal.experiment;
  };

  that.setExperiment = function storeExperiment (experiment) {
    internal.experiment = experiment;
  };

  that.clearExperiment = function clearExperiment () {
    internal.experiment = null;
  };

  return that;
}

exports.function = ExperimentService;

exports.inject = function (module) {
  module.service('ExperimentService', ExperimentService);
};
