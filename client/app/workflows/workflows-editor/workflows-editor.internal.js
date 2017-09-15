let internal = {};

(function() {
  'use strict';

  internal.createNode = function createNode(params) {
    params.id = this.UUIDGenerator.generateUUID();
    return this.WorkflowService.getWorkflow().createNode(params);
  };

  internal.createNodeAndAdd = function createNodeAndAdd(params) {
    return this.WorkflowService.getWorkflow().addNode(internal.createNode.call(this, params));
  };

})();

export default internal;
