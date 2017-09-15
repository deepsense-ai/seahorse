'use strict';

class NodeCopyPasteVisitorService {
  /* @ngInject */
  constructor($q, $rootScope, MultiSelectionService, WorkflowService, GraphNodesService) {
    _.assign(this, {$q, $rootScope, MultiSelectionService, WorkflowService, GraphNodesService});
  }

  getType() {
    return 'nodes';
  }

  isThereAnythingToCopy() {
    return this.MultiSelectionService.getSelectedNodeIds().length > 0;
  }

  getSerializedDataToCopy() {
    let nodeIds = this.MultiSelectionService.getSelectedNodeIds();
    let nodeIdsJoined = nodeIds.join();
    return nodeIdsJoined;
  }

  isFocused() {
    return $('.flowchart-box').is(':focus');
  }

  pasteUsingSerializedData(serializedData) {
    let nodeIds = serializedData.split(',');
    let workflow = this.WorkflowService.getCurrentWorkflow();
    let nodes = nodeIds.map(workflow.getNodeById);

    let nodeParametersPromises = _.map(nodes, node => {
      return this.GraphNodesService.getNodeParameters(node);
    });

    this.$q.all(nodeParametersPromises).then(
      nodes => {
        const legalNodesToPaste = _.filter(nodes, n => !this.GraphNodesService.isSinkOrSource(n));
        return this.GraphNodesService.cloneNodes(workflow, legalNodesToPaste)
      }
    ).then((clonedNodes) => {
      // mark clones as selected after they are created
      this.$rootScope.$applyAsync(() => {
        let nodesId = clonedNodes.map(node => node.id);
        this.MultiSelectionService.clearSelection();
        this.$rootScope.$broadcast('MultiSelection.ADD', nodesId);
      });

      this.$rootScope.$broadcast('INTERACTION-PANEL.FIT', {
        zoomId: 'flowchart-box'
      });
    });
  }

}

exports.inject = function(module) {
  module.service('NodeCopyPasteVisitorService', NodeCopyPasteVisitorService);
};
