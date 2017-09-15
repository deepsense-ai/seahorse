let internal = {};

(function() {
  'use strict';

  internal.setEdgeConnectionFromClone = function setEdgeConnectionFromClone(
    node, clones, cloningNodeIds
  ) {
    _.filter(node.edges, edge => edge.startNodeId !== node.id)
      .forEach(edge => {
        let cloningNodeIndex = cloningNodeIds.indexOf(edge.startNodeId);

        if (cloningNodeIndex > -1) {
          edge.__connectFromClone = clones[cloningNodeIndex].id;
        }
      });
  };

  internal.getNodeParameters = function getNodeParameters(node) {
    let deferred = this.$q.defer();

    if (node.hasParameters()) {
      deferred.resolve(node, 'sync');
    } else {
      this.Operations.getWithParams(node.operationId)
        .then(operationData => {
          this.$scope.$applyAsync(() => {
            node.setParameters(operationData.parameters, this.DeepsenseNodeParameters);
            deferred.resolve(node, 'async');
          });
        }, (error) => {
          console.error('operation fetch error', error);
          deferred.reject(error);
        });
    }

    return deferred.promise;
  };

  internal.createNode = function createNode(params) {
    params.id = this.UUIDGenerator.generateUUID();
    return this.WorkflowService.getWorkflow().createNode(params);
  };

  internal.createNodeAndAdd = function createNodeAndAdd(params) {
    return this.WorkflowService.getWorkflow().addNode(internal.createNode.call(this, params));
  };

  internal.cloneNodes = function cloneNodes(nodes) {
    let cloningNodeIds = nodes.map(node => node.id);
    let clones = [];

    // create new nodes
    _.forEach(nodes, node => clones.push(this.WorkflowService.cloneNode(node)));

    // clone connections with saving hierarchy
    _.forEach(nodes, (node, index) => {
      internal.setEdgeConnectionFromClone(
        node, clones, cloningNodeIds
      );
      this.WorkflowService.getWorkflow().cloneEdges(node, clones[index]);
    });

    // mark clones as selected after they are created
    this.$scope.$applyAsync(() => {
      let nodesId = clones.map(node => node.id);
      this.unselectNode();
      this.MultiSelectionService.clearSelection();
      this.MultiSelectionService.addNodesToSelection(nodesId);
      this.$rootScope.$broadcast('MultiSelection.ADD', nodesId);
    });

    this.WorkflowService.saveWorkflow();
  };
})();

export default internal;
