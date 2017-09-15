/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function ExperimentService(OperationsHierarchyService, Workflow) {
  let that = this;
  let internal = {
    experiment: null
  };

  that.createExperiment = function createExperiment(data, operations) {
    let experiment = new Workflow();

    experiment.setData({
      'id': data.id,
      'name': data.thirdPartyData.gui.name,
      'description': data.thirdPartyData.gui.description
    });
    experiment.createNodes(data.workflow.nodes, operations, data.thirdPartyData);
    experiment.createEdges(data.workflow.connections);
    //experiment.updateState(data.experiment.state);
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
    internal.experiment.updateTypeKnowledge(data.experiment.knowledge);
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
