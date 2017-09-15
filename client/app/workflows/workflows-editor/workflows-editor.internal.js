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

  internal.createNode = function createNode(params) {
    params.id = this.UUIDGenerator.generateUUID();
    return this.WorkflowService.getWorkflow().createNode(params);
  };

  internal.createNodeAndAdd = function createNodeAndAdd(params) {
    return this.WorkflowService.getWorkflow().addNode(internal.createNode.call(this, params));
  };

})();

export default internal;
