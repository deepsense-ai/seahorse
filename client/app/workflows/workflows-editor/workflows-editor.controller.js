'use strict';

import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';

/* @ngInject */
function WorkflowsEditorController(
  workflow,
  $scope, $state, $stateParams,
  GraphNode, Edge,
  PageService, Operations, GraphPanelRendererService, WorkflowService, UUIDGenerator, MouseEvent,
  DeepsenseNodeParameters, ConfirmationModalService, ExportModalService,
  RunModalFactory
) {
  let that = this;
  let internal = {};

  _.assign(that, {
    selectedNode: null,
    catalog: Operations.getCatalog()
  });

  internal.init = function init() {
    const DEFAULT_WORKFLOW_NAME = 'Draft workflow';
    let getTitle = () => {
      try {
        return workflow.thirdPartyData.gui.name || DEFAULT_WORKFLOW_NAME;
      } catch (e) {
        return DEFAULT_WORKFLOW_NAME;
      }
    };

    PageService.setTitle('Workflow: ' + getTitle());

    WorkflowService.createWorkflow(workflow, Operations.getData());
    GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
    GraphPanelRendererService.setZoom(1.0);

    internal.updateAndRerenderEdges(workflow);
  };

  internal.rerenderEdges = function rerenderEdges() {
    WorkflowService.updateEdgesStates();
    GraphPanelRendererService.changeEdgesPaintStyles();
  };

  internal.updateAndRerenderEdges = function updateAndRerenderEdges(data) {
    if (data && data.knowledge) {
      WorkflowService.updateTypeKnowledge(data.knowledge);
      internal.rerenderEdges();
    }
  };

  that.getWorkflow = WorkflowService.getWorkflow;

  that.getSelectedNode = function getSelectedNode() {
    return internal.selectedNode;
  };

  that.unselectNode = function unselectNode() {
    internal.selectedNode = null;
  };

  that.getGUIData = function getGUIData () {
    return workflow.thirdPartyData.gui;
  };

  $scope.$on('Workflow.SAVE.SUCCESS', (event, data) => {
    internal.updateAndRerenderEdges(data);
  });

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

  $scope.$on(Edge.CREATE, (data, args)  => {
    WorkflowService.getWorkflow().addEdge(args.edge);
  });

  $scope.$on(Edge.REMOVE, (data, args)  => {
    WorkflowService.getWorkflow().removeEdge(args.edge);
  });

  $scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
    if (internal.selectedNode) {
      WorkflowService.getWorkflow().removeNode(internal.selectedNode.id);
      GraphPanelRendererService.removeNode(internal.selectedNode.id);
      that.unselectNode();
      $scope.$digest();
      WorkflowService.saveWorkflow();
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
    WorkflowService.saveWorkflow();
  });

  $scope.$on('AttributePanel.UNSELECT_NODE', () => {
    that.unselectNode();
    $scope.$digest();
  });

  $scope.$on('$destroy', () => {
    WorkflowService.clearWorkflow();
    GraphPanelRendererService.clearWorkflow();
  });

  $scope.$on('StatusBar.SAVE_CLICK', () => {
    WorkflowService.saveWorkflow();
  });

  $scope.$on('StatusBar.HOME_CLICK', () => {
    ConfirmationModalService.showModal({
      message: 'The operation redirects to the home page. Make sure you saved the current state of the workflow.'
    }).
      then(() => {
        $state.go('home');
      });
  });

  $scope.$on('StatusBar.CLEAR_CLICK', () => {
    ConfirmationModalService.showModal({
      message: 'The operation clears the whole workflow graph and it cannot be undone afterwards.'
    }).
      then(() => {
        WorkflowService.clearGraph();
        GraphPanelRendererService.rerender();
        WorkflowService.saveWorkflow();
      });
  });

  $scope.$on('StatusBar.EXPORT_CLICK', () => {
    ExportModalService.showModal();
  });

  $scope.$on('StatusBar.RUN', () => {
    RunModalFactory.showModal({
      message: `Discovery Peak Apache Spark cluster`
    });
  });

  $scope.$on('StatusBar.LAST_EXECUTION_REPORT', () => {
    ConfirmationModalService.showModal({
      message: `The operation redirects to the view that displays the latest report for this workflow.
      The workflow had to be executed at least once. Make sure you saved the current state of the workflow.`
    }).
      then(() => {
        $state.go('workflows.latest_report', {
          'id': $stateParams.id
        });
      });
  });

  $scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
    if (newValue !== oldValue) {
      $scope.$applyAsync(() => {
        GraphPanelRendererService.rerender();
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

exports.inject = function (module) {
  module.controller('WorkflowsEditorController', WorkflowsEditorController);
};
