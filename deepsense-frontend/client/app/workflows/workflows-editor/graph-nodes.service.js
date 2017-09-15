'use strict';

class GraphNodesService {
  /* @ngInject */
  constructor($q, $rootScope, $timeout, DeepsenseNodeParameters, Operations, UUIDGenerator, nodeTypes) {
    _.assign(this, {$q, $rootScope, $timeout, DeepsenseNodeParameters, Operations, UUIDGenerator, nodeTypes});
  }

  getNodeParameters(node) {
    let deferred = this.$q.defer();

    if (node.hasParameters()) {
      node.refreshParameters(this.DeepsenseNodeParameters);
      this.$timeout(() => {
        // Sometimes when we click on a port to show report, '$digest cycle in progress' is shown. This fixes it.
        this.$rootScope.$apply();
      }, 0);
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

  createNodeAndAdd(workflow, params) {
    params.id = this.UUIDGenerator.generateUUID();
    let node = workflow.createNode(params);
    workflow.addNode(node);
    return node;
  }

  cloneNodes(workflow, nodes) {
    let cloningNodeIds = nodes.map(node => node.id);
    let clones = _.map(nodes, node => this._cloneNode(workflow, node));

    // clone connections with saving hierarchy
    _.forEach(nodes, (node, index) => {
      this._setEdgeConnectionFromClone(node, clones, cloningNodeIds);
      workflow.cloneEdges(node, clones[index]);
    });
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

    const createdNode = workflow.createNode(nodeParams);
    createdNode.parametersValues = angular.copy(node.parameters.serialize());
    if (node.hasInnerWorkflow()) {
      let innerWorkflow = createdNode.getInnerWorkflow();
      const map = this._mapOldIdsWithNewOnes(innerWorkflow);
      innerWorkflow = this._assignNewIds(map, innerWorkflow);
      createdNode.setInnerWorkflow(innerWorkflow);
    }
    return workflow.addNode(createdNode);
  }

  _mapOldIdsWithNewOnes(innerWorkflow) {
    let map = {};
    innerWorkflow.nodes.forEach((node) => {
      if (node.operation.id === this.nodeTypes.CUSTOM_TRANSFORMER) {
        let mapFromNestedNode = this._mapOldIdsWithNewOnes(node.parameters['inner workflow'].workflow);
        map = angular.extend(map, mapFromNestedNode);
      }
      map[node.id] = this.UUIDGenerator.generateUUID();
    });
    return map;
  }

  _assignNewIds(map, innerWorkflow) {
    let newInnerWorkflow = angular.copy(innerWorkflow);
    newInnerWorkflow.connections.forEach((connection) => {
      connection.from.nodeId = map[connection.from.nodeId];
      connection.to.nodeId = map[connection.to.nodeId];
    });
    newInnerWorkflow.nodes.forEach((node) => {
      if(_.has(node.parameters, 'inner workflow')) {
        node.parameters['inner workflow'].workflow = this._assignNewIds(map, node.parameters['inner workflow'].workflow);
      }
      node.id = map[node.id];
    });
    return newInnerWorkflow;
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
