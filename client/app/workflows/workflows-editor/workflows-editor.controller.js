'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */
import internal from './workflows-editor.internal.js';

class WorkflowsEditorController {

  /* @ngInject */
  constructor(workflow, config, MultiSelectionService,
    $scope, $state, $stateParams, $q, $rootScope,
    GraphNode, Edge,
    PageService, Operations, GraphPanelRendererService, WorkflowService, UUIDGenerator, MouseEvent,
    DeepsenseNodeParameters, ConfirmationModalService, ExportModalService,
    NotificationService, ServerCommunication, CopyPasteService) {
    this.ServerCommunication = ServerCommunication;
    this.PageService = PageService;
    this.WorkflowService = WorkflowService;
    this.DeepsenseNodeParameters = DeepsenseNodeParameters;
    this.Edge = Edge;
    this.config = config;
    this.workflow = workflow;
    this.MultiSelectionService = MultiSelectionService;
    this.$scope = $scope;
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.$q = $q;
    this.NotificationService = NotificationService;
    this.GraphPanelRendererService = GraphPanelRendererService;
    this.ConfirmationModalService = ConfirmationModalService;
    this.ExportModalService = ExportModalService;
    this.MouseEvent = MouseEvent;
    this.UUIDGenerator = UUIDGenerator;
    this.GraphNode = GraphNode;
    this.selectedNode = null;
    this.Operations = Operations;
    this.catalog = Operations.getCatalog();
    this.isReportMode = false;
    this.eventListeners = [];
    this.zoomId = 'flowchart-box';
    this.CopyPasteService = CopyPasteService;
    this.multipleCopyParams = {
      type: 'nodes',
      filter: () => MultiSelectionService.getSelectedNodes().length,
      paste: (event, nodes) => {
        let nodeParametersPromises = _.map(nodes, node => {
          return internal.getNodeParameters.call(this, node);
        });

        return $q.all(nodeParametersPromises).then(
          nodes => internal.cloneNodes.call(this, nodes)
        ).then(
          () => $scope.$broadcast('INTERACTION-PANEL.FIT', {
            zoomId: this.zoomId
          })
        );
      },
      getEntityToCopy: () => MultiSelectionService.getSelectedNodes()
        .map(WorkflowService.getWorkflow().getNodeById)
    };

    this.init();
  }

  init() {
    this.PageService.setTitle('Workflow editor');
    this.WorkflowService.createWorkflow(this.workflow, this.Operations.getData());
    this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
    this.GraphPanelRendererService.setZoom(1.0);
    this.CopyPasteService.add(this.multipleCopyParams);
    this.updateAndRerenderEdges(this.workflow);
    this.initListeners();
    this.ServerCommunication.init();
  }

  initListeners() {
    this.$scope.$on('ServerCommunication.MESSAGE.executionStatus', (event, data) => {
      this.WorkflowService.getWorkflow().updateState(data);
    });

    this.$scope.$on('StatusBar.RUN', () => {
      this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.REPORT_RENDER_MODE);
      this.GraphPanelRendererService.rerender();
      this.unbindListeners();
      this.isReportMode = true;
      this.isRunning = true;
      let serialized = this.WorkflowService.getWorkflow().serialize();
      let nodesToExecute = this.MultiSelectionService.getSelectedNodes();
      this.ServerCommunication.launch(this.workflow.id, serialized, nodesToExecute);
    });

    this.$scope.$on('StatusBar.ABORT', () => {
      this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
      this.GraphPanelRendererService.rerender();
      this.initUnbindableListeners();
      this.isReportMode = false;
      this.isRunning = false;
      this.ServerCommunication.unSubscribeRabbit(this.workflow.id);
    });

    this.$scope.$on('GraphNode.CLICK', (event, data) => {
      this.selectedNode = data.selectedNode;
      internal.getNodeParameters.call(this, this.selectedNode);
    });

    this.initUnbindableListeners();
  }

  initUnbindableListeners() {
    this.eventListeners = [
      this.$scope.$on('Workflow.SAVE.SUCCESS', (event, data) => {
        this.updateAndRerenderEdges(data);
      }),

      this.$scope.$on(this.GraphNode.MOVE, () => {
        this.WorkflowService.saveWorkflow();
      }),

      this.$scope.$on(this.Edge.CREATE, (data, args) => {
        this.WorkflowService.getWorkflow().addEdge(args.edge);
      }),

      this.$scope.$on(this.Edge.REMOVE, (data, args) => {
        this.WorkflowService.getWorkflow().removeEdge(args.edge);
      }),

      this.$scope.$on('FlowChartBox.ELEMENT_DROPPED', (event, args) => {
        let dropElementOffset = this.MouseEvent.getEventOffsetOfElement(args.dropEvent, args.target);
        let operation = this.Operations.get(args.elementId);
        let offsetX = dropElementOffset.x;
        let offsetY = dropElementOffset.y;
        let positionX = offsetX || 0;
        let positionY = offsetY || 0;
        let elementOffsetX = 100;
        let elementOffsetY = 30;
        let node = internal.createNodeAndAdd.call(this, {
          operation: operation,
          // TODO check if we reached right and bottom end of flowchart box,
          x: positionX > elementOffsetX ? positionX - elementOffsetX : 0,
          y: positionY > elementOffsetY ? positionY - elementOffsetY : 0
        });

        internal.getNodeParameters.call(this, node);
      }),

      this.$scope.$on('AttributePanel.UNSELECT_NODE', () => {
        this.unselectNode();
        this.$scope.$digest();
      }),

      this.$scope.$on('AttributesPanel.UPDATED', () => {
        this.WorkflowService.saveWorkflow();
      }),

      this.$scope.$on('StatusBar.HOME_CLICK', () => {
        let url = this.$state.href('home');
        window.open(url, '_blank');
      }),

      this.$scope.$on('StatusBar.CLEAR_CLICK', () => {
        this.ConfirmationModalService.showModal({
          message: 'The operation clears the whole workflow graph and it cannot be undone afterwards.'
        }).
        then(() => {
          this.WorkflowService.clearGraph();
          this.GraphPanelRendererService.rerender();
          this.WorkflowService.saveWorkflow();
        });
      }),

      this.$scope.$on('StatusBar.EXPORT_CLICK', () => {
        this.ExportModalService.showModal();
      }),

      this.$scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
        this.WorkflowService.getWorkflow().removeNodes(this.MultiSelectionService.getSelectedNodes());
        this.GraphPanelRendererService.removeNodes(this.MultiSelectionService.getSelectedNodes());
        this.MultiSelectionService.clearSelection();
        this.unselectNode();
        this.$scope.$apply();
        this.WorkflowService.saveWorkflow();
      }),

      this.$scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          this.$scope.$applyAsync(() => {
            this.GraphPanelRendererService.rerender();
          });
        }
      }),

      this.$scope.$watchCollection('workflow.getWorkflow().getEdgesIds()', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          this.$scope.$applyAsync(() => {
            this.rerenderEdges();
          });
        }
      }),

      this.$scope.$on('$destroy', () => {
        this.WorkflowService.clearWorkflow();
        this.GraphPanelRendererService.clearWorkflow();
        this.LastExecutionReportService.clearTimeout();
        this.NotificationService.clearToasts();
      })
    ];
  }

  unbindListeners() {
    this.eventListeners.forEach(func => func());
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
