/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Experiment = require('./valueObject/experiment.js');

function ExperimentFactory() {

  var that = this;

  that.createExperiment = function createExperiment(data, operations) {
    var experiment = new Experiment();
    experiment.createNodes(data.experiment.graph.nodes, operations);
    experiment.createConnections(data.experiment.graph.edges);
    return experiment;
  };

  return that;
}

exports.function = ExperimentFactory;

exports.inject = function (module) {
  module.service('ExperimentFactory', ExperimentFactory);
};
