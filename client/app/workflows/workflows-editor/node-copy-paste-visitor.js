'use strict';

class NodeCopyPasteVisitor {

  // TODO Use ng-inject and remake it into service.
  // First untangle controller (WorkflowService) dependencies (split into services)
  constructor(MultiSelectionService, $q, $rootScope, WorkflowService, WorkflowsEditorController, GraphNodesService) {
    this.MultiSelectionService = MultiSelectionService;
    this.$q = $q;
    this.$rootScope = $rootScope;
    this.WorkflowService = WorkflowService;
    this.WorkflowsEditorController = WorkflowsEditorController;
    this.GraphNodesService = GraphNodesService;
  }

  getType() {
    return 'nodes';
  }

  isThereAnythingToCopy() {
    return this.MultiSelectionService.getSelectedNodes().length > 0;
  }

  getSerializedDataToCopy() {
    let nodeIds = this.MultiSelectionService.getSelectedNodes();
    let nodeIdsJoined = nodeIds.join();
    return nodeIdsJoined;
  }

  isFocused() {
    return $('.flowchart-box').is(':focus');
  }

  pasteUsingSerializedData(serializedData) {
    let nodeIds = serializedData.split(',');
    let workflow = this.WorkflowService.getWorkflow();
    let nodes = nodeIds.map(workflow.getNodeById);

    let nodeParametersPromises = _.map(nodes, node => {
      return this.GraphNodesService.getNodeParameters(node);
    });

    this.$q.all(nodeParametersPromises).then(
      nodes => this.GraphNodesService.cloneNodes(workflow, nodes)
    ).then((clonedNodes) => {
      // mark clones as selected after they are created
      this.$rootScope.$applyAsync(() => {
        let nodesId = clonedNodes.map(node => node.id);
        this.WorkflowsEditorController.unselectNode();
        this.MultiSelectionService.clearSelection();
        this.MultiSelectionService.addNodesToSelection(nodesId);
        this.$rootScope.$broadcast('MultiSelection.ADD', nodesId);
      });

      this.$rootScope.$broadcast('INTERACTION-PANEL.FIT', {
        zoomId: this.WorkflowsEditorController.zoomId
      });
    });
  }

}

export default NodeCopyPasteVisitor;
