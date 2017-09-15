/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function ExperimentService(OperationsHierarchyService, Experiment) {
  var that = this;
  var internal = {
    experiment: null
  };

  that.createExperiment = function createExperiment(data, operations) {
    var experiment = new Experiment();

    experiment.setData(data.experiment);
    experiment.createNodes(data.experiment.graph.nodes, operations, data.experiment.state);
    experiment.createEdges(data.experiment.graph.edges);
    experiment.updateState(data.experiment.state);
    experiment.updateEdgesStates(OperationsHierarchyService);

    return experiment;
  };

  that.getExperiment = function getExperiment () {
    return internal.experiment;
  };

  that.setExperiment = function storeExperiment (experiment) {
    internal.experiment = experiment;
  };

  that.clearExperiment = function clearExperiment() {
    internal.experiment = null;
  };

  that.updateTypeKnowledge = function updateTypeKnowledge (data) {
    internal.experiment.updateTypeKnowledge(data.experiment.typeKnowledge);
  };

  that.updateEdgesStates = function () {
    internal.experiment.updateEdgesStates(OperationsHierarchyService);
  };

  that.experimentIsSet = function experimentIsSet () {
    return !_.isNull(internal.experiment);
  };

  return that;
}

exports.function = ExperimentService;

exports.inject = function (module) {
  module.service('ExperimentService', ExperimentService);
};
