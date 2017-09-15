'use strict';

class GraphNodesService {

  /* @ngInject */
  constructor($q, DeepsenseNodeParameters, $rootScope, Operations, WorkflowService, MultiSelectionService,
              UUIDGenerator) {
    this.$q = $q;
    this.DeepsenseNodeParameters = DeepsenseNodeParameters;
    this.$rootScope = $rootScope;
    this.Operations = Operations;
    this.WorkflowService = WorkflowService;
    this.MultiSelectionService = MultiSelectionService;
    this.UUIDGenerator = UUIDGenerator;
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

  cloneNodes(workflow, nodes) {
    let cloningNodeIds = nodes.map(node => node.id);

    let clones = _.map(nodes, node => this._cloneNode(workflow, node));

    // clone connections with saving hierarchy
    _.forEach(nodes, (node, index) => {
      this._setEdgeConnectionFromClone(node, clones, cloningNodeIds);
      workflow.cloneEdges(node, clones[index]);
    });

    this.WorkflowService.saveWorkflow();
    return clones;
  }

  // TODO This (and cloneNodes) should probably be part of workflow class and added to project deepsense-graph-model
  _cloneNode(workflow, node) {
    let operation = this.Operations.get(node.operationId);
    let offset = {
      x: 255,
      y: 0
    };
    let nodeClone = _.cloneDeep(node);
    let nodeParams = angular.merge(
      nodeClone, {
        'id': this.UUIDGenerator.generateUUID(),
        'operation': operation,
        'x': node.x - offset.x >= 0 ? node.x - offset.x : node.x,
        'y': node.y - offset.y >= 0 ? node.y - offset.y : node.y,
        'uiName': nodeClone.uiName ? nodeClone.uiName += ' copy' : ''
      }
    );
    let createdNode = workflow.createNode(nodeParams);
    createdNode.parametersValues = node.parameters.serialize();
    return workflow.addNode(createdNode);
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
