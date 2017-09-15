'use strict';

/* @ngInject */
function WorkflowsReportController(
  $state, $scope, report, ConfirmationModalService,
  GraphNode, PageService, Operations, GraphPanelRendererService, WorkflowService, DeepsenseNodeParameters
) {
  let that = this;
  let internal = {};

  _.assign(internal, {
    selectedNode: null,
    unselectNode: () => { internal.selectedNode = null; }
  });

  _.assign(that, {
    getWorkflow: WorkflowService.getWorkflow,
    getSelectedNode: () => internal.selectedNode
  });

  internal.init = function init() {
    let workflow = report;

    const DEFAULT_WORKFLOW_NAME = 'Draft workflow';
    let getTitle = () => {
      try {
        return workflow.thirdPartyData.gui.name || DEFAULT_WORKFLOW_NAME;
      } catch (e) {
        return DEFAULT_WORKFLOW_NAME;
      }
    };

    PageService.setTitle(`Workflow execution report: ${getTitle()}`);

    WorkflowService.createWorkflow(workflow, Operations.getData());
    GraphPanelRendererService.setWorkflow(WorkflowService.getWorkflow());
    GraphPanelRendererService.setZoom(1.0);

    GraphPanelRendererService.changeEdgesPaintStyles();
  };

  $scope.$on(GraphNode.CLICK, (event, data) => {
    let node = data.selectedNode;

    internal.selectedNode = node;

    if (node.hasParameters()) {
      $scope.$digest();
    } else {
      Operations.getWithParams(node.operationId).then((operationData) => {
        $scope.$applyAsync(() => {
          node.setParameters(operationData.parameters, DeepsenseNodeParameters);
        });
      }, (error) => {
        console.error('operation fetch error', error);
      });
    }
  });

  $scope.$on('AttributePanel.UNSELECT_NODE', () => {
    internal.unselectNode();
    $scope.$digest();
  });

  $scope.$on('StatusBar.HOME_CLICK', () => {
    ConfirmationModalService.showModal({
      message: 'The operation redirects to the home page.'
    }).
      then(() => {
        $state.go('home');
      });
  });

  $scope.$on('$destroy', () => {
    WorkflowService.clearWorkflow();
    GraphPanelRendererService.clearWorkflow();
  });

  internal.init();

  return that;
}

exports.function = WorkflowsReportController;

exports.inject = function (module) {
  module.controller('WorkflowsReportController', WorkflowsReportController);
};
