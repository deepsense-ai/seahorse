'use strict';

class NodeCopyPasteVisitor {

  constructor(MultiSelectionService, $q, $rootScope, WorkflowService, WorkflowsEditorController) {
    this.MultiSelectionService = MultiSelectionService;
    this.$q = $q;
    this.$scope = $rootScope;
    this.WorkflowService = WorkflowService;
    this.WorkflowsEditorController = WorkflowsEditorController;
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
    let nodes = nodeIds.map(this.WorkflowService.getWorkflow().getNodeById);

    let nodeParametersPromises = _.map(nodes, node => {
      return this.WorkflowsEditorController.getNodeParameters(node);
    });

    this.$q.all(nodeParametersPromises).then(
      nodes => this.WorkflowsEditorController.cloneNodes(nodes)
    ).then(
      () => this.$scope.$broadcast('INTERACTION-PANEL.FIT', {
        zoomId: this.WorkflowsEditorController.zoomId
      })
    );
  }

}

export default NodeCopyPasteVisitor;
