/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function WorkflowsController(
  workflow,
  $scope, $timeout,
  GraphNode, Edge,
  PageService, Operations, GraphPanelRendererService, WorkflowService,
  WorkflowsApiClient, UUIDGenerator, MouseEvent,
  DeepsenseNodeParameters
) {
  let that = this;
  let internal = {};

  _.assign(that, {
    selectedNode: null,
    catalog: Operations.getCatalog()
  });

  internal.init = function init() {
    PageService.setTitle('Workflow: ' + workflow.thirdPartyData.gui.name);

    WorkflowService.createWorkflow(workflow, Operations.getData());
    GraphPanelRendererService.setWorkflow(WorkflowService.getWorkflow());
    GraphPanelRendererService.setZoom(1.0);

    //internal.updateAndRerenderEdges(experiment);

    $scope.$on('FlowChartBox.Rendered', () => {
      $timeout(() => {
        that.updateFlowchartBox();
      }, 0, false);
    });
  };

  internal.rerenderEdges = function rerenderEdges() {
    WorkflowService.updateEdgesStates();
    GraphPanelRendererService.changeEdgesPaintStyles();
  };

  internal.updateAndRerenderEdges = function updateAndRerenderEdges(data) {
    WorkflowService.updateTypeKnowledge(data);
    internal.rerenderEdges();
  };

  internal.saveWorkflow = function saveWorkflow() {
    let serializedWorkflow = WorkflowService.getWorkflow().serialize();
    return WorkflowsApiClient.
      updateWorkflow(serializedWorkflow).
      then((result) => {
        if (WorkflowService.workflowIsSet()) {
          //internal.updateAndRerenderEdges(result);
          $scope.$emit('Workflow.SAVE.SUCCESS');
        }
      }, (error) => {
        $scope.$emit('Workflow.SAVE.ERROR', error);
      });
  };

  that.updateFlowchartBox = function updateFlowchartBox() {
    GraphPanelRendererService.init();
    GraphPanelRendererService.renderPorts();
    GraphPanelRendererService.renderEdges();
    GraphPanelRendererService.repaintEverything();
    $scope.$broadcast('Workflow.RENDER_FINISHED');
  };

  that.getWorkflow = WorkflowService.getWorkflow;

  that.getSelectedNode = function getSelectedNode() {
    return internal.selectedNode;
  };

  that.unselectNode = function unselectNode() {
    internal.selectedNode = null;
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

  $scope.$on('Workflow.SAVE', () => {
    internal.saveWorkflow();
  });

  $scope.$on('StatusBar.CLEAR_CLICK',() => {
    WorkflowService.clearGraph();
    that.updateFlowchartBox();
    internal.saveWorkflow();
  });

  $scope.$on(Edge.CREATE, (data, args)  => {
    WorkflowService.getWorkflow().addEdge(args.edge);
    internal.saveWorkflow();
  });

  $scope.$on(Edge.REMOVE, (data, args)  => {
    WorkflowService.getWorkflow().removeEdge(args.edge);
    internal.saveWorkflow();
  });

  $scope.$on('Keyboard.KEY_PRESSED_DEL', (event, data) => {
    if (internal.selectedNode) {
      WorkflowService.getWorkflow().removeNode(internal.selectedNode.id);
      GraphPanelRendererService.removeNode(internal.selectedNode.id);
      that.unselectNode();
      $scope.$digest();
      internal.saveWorkflow();
    }
  });

  $scope.$on('FlowChartBox.ELEMENT_DROPPED', function elementDropped(event, args) {
    let dropElementOffset = MouseEvent.getEventOffsetOfElement(args.dropEvent, args.target);
    let operation = Operations.get(args.elementId);
    let offsetX = dropElementOffset.x;
    let offsetY = dropElementOffset.y;
    let positionX = offsetX || 0;
    let positionY = offsetY || 0;
    let elementOffsetX = 100;
    let elementOffsetY = 30;
    let node = WorkflowService.getWorkflow().createNode({
        'id': UUIDGenerator.generateUUID(),
        'operation': operation,
        'x': positionX > elementOffsetX ? positionX - elementOffsetX : 0,
        'y': positionY > elementOffsetY ? positionY - elementOffsetY : 0
      });

    WorkflowService.getWorkflow().addNode(node);
    internal.saveWorkflow();
  });

  $scope.$on('AttributePanel.UNSELECT_NODE', () => {
    that.unselectNode();
    $scope.$digest();
  });

  $scope.$on('$destroy', () => {
    WorkflowService.clearWorkflow();
    GraphPanelRendererService.clearWorkflow();
  });

  $scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
    if (newValue !== oldValue) {
      $scope.$applyAsync(() => {
        that.updateFlowchartBox();
      });
    }
  });

  $scope.$watchCollection('workflow.getWorkflow().getEdgesIds()', (newValue, oldValue) => {
    if (newValue !== oldValue) {
      $scope.$applyAsync(() => {
        internal.rerenderEdges();
      });
    }
  });

  internal.init();

  return that;
}

exports.function = WorkflowsController;

exports.inject = function (module) {
  module.controller('WorkflowsController', WorkflowsController);
};
