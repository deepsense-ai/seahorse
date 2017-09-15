'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */

class WorkflowsEditorController {

  /* @ngInject */
  constructor(workflow, MultiSelectionService,
    $scope, $state, $stateParams,
    GraphNode, Edge,
    PageService, Operations, GraphPanelRendererService, WorkflowService, UUIDGenerator, MouseEvent,
    DeepsenseNodeParameters, ConfirmationModalService, ExportModalService,
    LastExecutionReportService, NotificationService, ServerCommunication) {
    this.ServerCommunication = ServerCommunication;
    this.PageService = PageService;
    this.WorkflowService = WorkflowService;
    this.DeepsenseNodeParameters = DeepsenseNodeParameters;
    this.Edge = Edge;
    this.workflow = workflow;
    this.MultiSelectionService = MultiSelectionService;
    this.$scope = $scope;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.NotificationService = NotificationService;
    this.GraphPanelRendererService = GraphPanelRendererService;
    this.ConfirmationModalService = ConfirmationModalService;
    this.LastExecutionReportService = LastExecutionReportService;
    this.ExportModalService = ExportModalService;
    this.MouseEvent = MouseEvent;
    this.UUIDGenerator = UUIDGenerator;
    this.GraphNode = GraphNode;
    this.selectedNode = null;
    this.Operations = Operations;
    this.catalog = Operations.getCatalog();
    this.init();
  }

  init() {
    this.PageService.setTitle('Workflow editor');
    this.WorkflowService.createWorkflow(this.workflow, this.Operations.getData());
    this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
    this.GraphPanelRendererService.setZoom(1.0);
    this.LastExecutionReportService.setTimeout();
    this.updateAndRerenderEdges(this.workflow);
    this.initListeners();
  }

  initListeners() {

    this.$scope.$on('Workflow.SAVE.SUCCESS', (event, data) => {
      this.updateAndRerenderEdges(data);
    });

    this.$scope.$on('GraphNode.CLICK', (event, data) => {
      let node = data.selectedNode;
      this.selectedNode = node;
      if (!_.includes(this.MultiSelectionService.getSelectedNodes(), node.id)) {
        this.MultiSelectionService.setSelectedNodes([node.id]);
      }
      if (node.hasParameters()) {
        this.$scope.$digest();
      } else {
        this.Operations.getWithParams(node.operationId)
          .then((operationData) => {
            this.$scope.$applyAsync(() => {
              node.setParameters(operationData.parameters, this.DeepsenseNodeParameters);
            });
          }, (error) => {
            console.error('operation fetch error', error);
          });
      }
    });

    this.$scope.$on(this.GraphNode.MOVE, () => {
      this.WorkflowService.saveWorkflow();
    });

    this.$scope.$on(this.Edge.CREATE, (data, args) => {
      this.WorkflowService.getWorkflow().addEdge(args.edge);
    });

    this.$scope.$on(this.Edge.REMOVE, (data, args) => {
      this.WorkflowService.getWorkflow().removeEdge(args.edge);
    });

    this.$scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
      this.WorkflowService.getWorkflow().removeNodes(this.MultiSelectionService.getSelectedNodes());
      this.GraphPanelRendererService.removeNodes(this.MultiSelectionService.getSelectedNodes());
      this.MultiSelectionService.clearSelection();
      this.unselectNode();
      this.$scope.$apply();
      this.WorkflowService.saveWorkflow();
    });

    this.$scope.$on('FlowChartBox.ELEMENT_DROPPED', (event, args) => {
      let dropElementOffset = this.MouseEvent.getEventOffsetOfElement(args.dropEvent, args.target);
      let operation = this.Operations.get(args.elementId);
      let offsetX = dropElementOffset.x;
      let offsetY = dropElementOffset.y;
      let positionX = offsetX || 0;
      let positionY = offsetY || 0;
      let elementOffsetX = 100;
      let elementOffsetY = 30;
      let node = this.WorkflowService.getWorkflow()
        .createNode({
          'id': this.UUIDGenerator.generateUUID(),
          'operation': operation,
          'x': positionX > elementOffsetX ? positionX - elementOffsetX : 0,
          'y': positionY > elementOffsetY ? positionY - elementOffsetY : 0
        });

      this.WorkflowService.getWorkflow().addNode(node);

      if (this.Operations.hasWithParams(operation.id)) {
        this.WorkflowService.saveWorkflow();
      } else {
        this.Operations.getWithParams(operation.id)
          .then((operationData) => {
            node.setParameters(operationData.parameters, this.DeepsenseNodeParameters);
            this.WorkflowService.saveWorkflow();
          }, (error) => {
            console.error('operation fetch error', error);
          });
      }
    });

    this.$scope.$on('AttributePanel.UNSELECT_NODE', () => {
      this.unselectNode();
      this.$scope.$digest();
    });

    this.$scope.$on('$destroy', () => {
      this.WorkflowService.clearWorkflow();
      this.GraphPanelRendererService.clearWorkflow();
      this.LastExecutionReportService.clearTimeout();
      this.NotificationService.clearToasts();
    });

    this.$scope.$on('AttributesPanel.UPDATED', () => {
      this.WorkflowService.saveWorkflow();
    });

    this.$scope.$on('StatusBar.HOME_CLICK', () => {
      let url = this.$state.href('home');
      window.open(url, '_blank');
    });

    this.$scope.$on('StatusBar.SAVE_CLICK', () => {
      this.WorkflowService.saveWorkflow();
    });

    this.$scope.$on('StatusBar.CLEAR_CLICK', () => {
      this.ConfirmationModalService.showModal({
        message: 'The operation clears the whole workflow graph and it cannot be undone afterwards.'
      }).
      then(() => {
        this.WorkflowService.clearGraph();
        this.GraphPanelRendererService.rerender();
        this.WorkflowService.saveWorkflow();
      });
    });

    this.$scope.$on('StatusBar.EXPORT_CLICK', () => {
      this.ExportModalService.showModal();
    });

    this.$scope.$on('StatusBar.RUN', () => {
      this.ServerCommunication.send(null, {}, JSON.stringify({
        messageType: 'run',
        messageBody: {
          workflowId: this.workflow.id
        }
      }));
    });

    this.$scope.$on('StatusBar.LAST_EXECUTION_REPORT', () => {
      let url = this.$state.href('workflows.latest_report', {
        'id': this.$stateParams.id
      });
      window.open(url, '_blank');
    });

    this.$scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.$scope.$applyAsync(() => {
          this.GraphPanelRendererService.rerender();
        });
      }
    });

    this.$scope.$watchCollection('workflow.getWorkflow().getEdgesIds()', (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.$scope.$applyAsync(() => {
          this.rerenderEdges();
        });
      }
    });
  }

  rerenderEdges() {
    this.WorkflowService.updateEdgesStates();
    this.GraphPanelRendererService.changeEdgesPaintStyles();
  }

  updateAndRerenderEdges(data) {
    if (data && data.knowledge) {
      this.WorkflowService.updateTypeKnowledge(data.knowledge);
      this.rerenderEdges();
    }
  }

  getWorkflow() {
    return this.WorkflowService.getWorkflow();
  }

  getPredefColors() {
    return this.WorkflowService.getPredefColors();
  }

  getSelectedNode() {
    return this.selectedNode;
  }

  unselectNode() {
    this.selectedNode = null;
  }
}

exports.inject = function(module) {
  module.controller('WorkflowsEditorController', WorkflowsEditorController);
};
