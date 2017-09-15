'use strict';

class GraphNodesService {

  /* @ngInject */
  constructor($q, DeepsenseNodeParameters, $rootScope, Operations, WorkflowService, MultiSelectionService) {
    this.$q = $q;
    this.DeepsenseNodeParameters = DeepsenseNodeParameters;
    this.$rootScope = $rootScope;
    this.Operations = Operations;
    this.WorkflowService = WorkflowService;
    this.MultiSelectionService = MultiSelectionService;
  }

  getNodeParameters(node) {
    let deferred = this.$q.defer();

    if (node.hasParameters()) {
      node.refreshParameters(this.DeepsenseNodeParameters);
      this.$rootScope.$apply();
      deferred.resolve(node, 'sync');
    } else {
      this.Operations.getWithParams(node.operationId)
        .then(operationData => {
          this.$rootScope.$applyAsync(() => {
            node.setParameters(operationData.parameters, this.DeepsenseNodeParameters);
            deferred.resolve(node, 'async');
          });
        }, (error) => {
          console.error('operation fetch error', error);
          deferred.reject(error);
        });
    }
    return deferred.promise;
  }

  cloneNodes(nodes) {
    let cloningNodeIds = nodes.map(node => node.id);
    let clones = [];

    // create new nodes
    _.forEach(nodes, node => clones.push(this.WorkflowService.cloneNode(node)));

    // clone connections with saving hierarchy
    _.forEach(nodes, (node, index) => {
      this._setEdgeConnectionFromClone(node, clones, cloningNodeIds);
      this.WorkflowService.getWorkflow().cloneEdges(node, clones[index]);
    });

    this.WorkflowService.saveWorkflow();
    return clones;
  }

  _setEdgeConnectionFromClone(node, clones, cloningNodeIds) {
    _.filter(node.edges, edge => edge.startNodeId !== node.id)
      .forEach(edge => {
        let cloningNodeIndex = cloningNodeIds.indexOf(edge.startNodeId);

        if (cloningNodeIndex > -1) {
          edge.__connectFromClone = clones[cloningNodeIndex].id;
        }
      });
  }

}

exports.inject = function(module) {
  module.service('GraphNodesService', GraphNodesService);
};
