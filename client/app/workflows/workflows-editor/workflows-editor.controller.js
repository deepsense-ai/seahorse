'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */

import NodeCopyPasteVisitor from './node-copy-paste-visitor.js';

class WorkflowsEditorController {

  // TODO Try to use this instead of long constructors with boilerplate?
  // http://stackoverflow.com/questions/27529518/automatically-set-arguments-as-instance-properties-in-es6

  /* @ngInject */
  constructor(workflowWithResults, config, Report, MultiSelectionService,
    $scope, $state, $q, $rootScope, $log,
    GraphNode, Edge,
    PageService, Operations, GraphPanelRendererService, WorkflowService, MouseEvent,
    ConfirmationModalService, ExportModalService, GraphNodesService,
    NotificationService, ServerCommunication, CopyPasteService, SideBarService, BottomBarService, WorkflowStatusBarService) {

    this.Report = Report;
    this.ServerCommunication = ServerCommunication;
    this.PageService = PageService;
    this.WorkflowService = WorkflowService;
    this.Edge = Edge;
    this.config = config;
    this.MultiSelectionService = MultiSelectionService;
    this.$scope = $scope;
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.$q = $q;
    this.$log = $log;
    this.NotificationService = NotificationService;
    this.GraphPanelRendererService = GraphPanelRendererService;
    this.ConfirmationModalService = ConfirmationModalService;
    this.ExportModalService = ExportModalService;
    this.MouseEvent = MouseEvent;
    this.GraphNode = GraphNode;
    this.selectedNode = null;
    this.Operations = Operations;
    this.catalog = Operations.getCatalog();
    this.isReportMode = false;
    this.eventListeners = [];
    this.zoomId = 'flowchart-box';
    this.CopyPasteService = CopyPasteService;
    this.SideBarData = SideBarService.data;
    this.BottomBarData = BottomBarService.tabsState;
    this.WorkflowStatusBarService = WorkflowStatusBarService;
    this.GraphNodesService = GraphNodesService;
    this.workflow = null;

    this.nodeCopyPasteVisitor = new NodeCopyPasteVisitor(MultiSelectionService, $q,
      $scope, WorkflowService, this, GraphNodesService);

    this.init(workflowWithResults[0]);
  }

  loadReports(data) {
    let report = data.resultEntities;
    if (!_.isEmpty(report)) {
      this.WorkflowService.getMainWorkflow().setPortTypesFromReport(report);
      this.Report.createReportEntities(report.id, report);
      this._initReportListeners();
      this.$scope.$applyAsync(() => {
        this.GraphPanelRendererService.rerender(this.workflow);
      });
    }
  }

  _initReportListeners() {
    if (this.inited) {
      return false;
    }

    this.$scope.$on('OutputPort.LEFT_CLICK', (event, data) => {
      let workflowId = data.workflowId;
      let workflow = this.WorkflowService.getWorkflowById(workflowId);
      let node = workflow.getNodeById(data.portObject.nodeId);

      this.MultiSelectionService.clearSelection();
      this.MultiSelectionService.addNodesToSelection([node.id]);
      this.workflowIdForReport = workflowId;
      this.nodeIdForReport = node.id;
      this.selectedNode = node;
      this.loadParametersForNode();

      let reportEntityId = node.getResult(data.reference.getParameter('portIndex'));

      if (this.Report.hasReportEntity(reportEntityId)) {
        this.Report.getReport(reportEntityId).then(report => {
          this.report = report;
          this.Report.openReport();
        });
      }
    });

    this.inited = true;
  }

  init(workflowWithResults) {
    this.PageService.setTitle('Workflow editor');
    this.workflow = this.WorkflowService.initMainWorkflow(workflowWithResults);
    this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
    this.GraphPanelRendererService.setZoom(1.0);
    this.CopyPasteService.registerCopyPasteVisitor(this.nodeCopyPasteVisitor);
    this.workflow.updateState(workflowWithResults.executionReport);
    this.initListeners();
    this.loadReports(workflowWithResults.executionReport);
  }

  initListeners() {
    this.$scope.$on('ServerCommunication.MESSAGE.ready', (event, data) => {
      this.$log.debug('Received a Ready message from Session Executor. Reconnecting.');
      this.ServerCommunication.reconnect();
    });

    this.$scope.$on('ServerCommunication.MESSAGE.executionStatus', (event, data) => {
      this.workflow.updateState(data);

      this.loadReports(data);

      if (!this.WorkflowService.isWorkflowRunning()) {
        this.$rootScope.$broadcast('ServerCommunication.EXECUTION_FINISHED');
      }
    });

    this.$scope.$on('ServerCommunication.MESSAGE.inferredState', (event, data) => {
      if (this.WorkflowService.workflowIsSet()) {
        this.$rootScope.$broadcast('Workflow.UPDATE.KNOWLEDGE', data);
        this.updateAndRerenderEdges(data);
      }
      if (data.states) {
        this.workflow.updateState(data.states);
      }
    });

    this.$scope.$on('StatusBar.RUN', () => {
      this.unbindListeners();
      this.isReportMode = true;
      this.isRunning = true;
      this.CopyPasteService.setEnabled(false);
      let nodesToExecute = this.MultiSelectionService.getSelectedNodes();
      this.ServerCommunication.sendLaunchToWorkflowExchange(nodesToExecute);
    });

    this.$scope.$on('AttributePanel.UNSELECT_NODE', () => {
      this.unselectNode();
      this.$scope.$digest();
    });

    this.$scope.$on('OpenReportTab.SELECT_NODE', () => {
      if (this.workflowIdForReport && this.nodeIdForReport) {
        let workflow = this.WorkflowService.getWorkflowById(this.workflowIdForReport);
        let node = workflow.getNodeById(this.nodeIdForReport);
        this.selectedNode = node;
        this.loadParametersForNode();
      }
    });

    this.$scope.$on('ServerCommunication.EXECUTION_FINISHED', () => {
      this.restoreEditableMode();
      this.isRunning = false;
      this.CopyPasteService.setEnabled(true);
    });

    this.$scope.$on('StatusBar.ABORT', () => {
      this.ServerCommunication.sendAbortToWorkflowExchange();
      this.restoreEditableMode();
      this.isRunning = false;
      this.CopyPasteService.setEnabled(true);
    });

    this.$scope.$on('GraphNode.CLICK', (event, data) => {
      if (!data.originalEvent.ctrlKey) {
        this.selectedNode = data.selectedNode;
        this.loadParametersForNode();
      } else if (data.originalEvent.ctrlKey && this.selectedNode && this.selectedNode.id === data.selectedNode.id) {
        this.unselectNode();
      }
    });

    this.initUnbindableListeners();
  }

  initUnbindableListeners() {
    this.eventListeners = [
      this.$scope.$on(this.Edge.CREATE, (data, args) => {
        this.workflow.addEdge(args.edge);
      }),

      this.$scope.$on(this.Edge.REMOVE, (data, args) => {
        this.workflow.removeEdge(args.edge);
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
        let node = this.GraphNodesService.createNodeAndAdd(this.workflow, {
          operation: operation,
          // TODO check if we reached right and bottom end of flowchart box,
          x: positionX > elementOffsetX ? positionX - elementOffsetX : 0,
          y: positionY > elementOffsetY ? positionY - elementOffsetY : 0
        });
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
          this.GraphPanelRendererService.rerender(this.workflow);
        });
      }),

      this.$scope.$on('StatusBar.EXPORT_CLICK', () => {
        this.ExportModalService.showModal();
      }),

      this.$scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
        this.workflow.removeNodes(this.MultiSelectionService.getSelectedNodes());
        this.GraphPanelRendererService.removeNodes(this.MultiSelectionService.getSelectedNodes());
        this.MultiSelectionService.clearSelection();
        this.unselectNode();
        this.$scope.$apply();
      }),

      this.$scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          this.$scope.$applyAsync(() => {
            this.GraphPanelRendererService.rerender(this.workflow);
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
        this.NotificationService.clearToasts();
      })
    ];
  }

  loadParametersForNode() {
    this.GraphNodesService.getNodeParameters(this.selectedNode).then((node, mode) => {
      if (mode === 'sync') {
        this.$scope.$digest();
      }
    });
  }

  restoreEditableMode() {
    this.initUnbindableListeners();
    this.isReportMode = false;
    this.WorkflowStatusBarService.createRunButton();
  }

  unbindListeners() {
    this.eventListeners.forEach(func => func());
  }

  rerenderEdges() {
    this.WorkflowService.updateEdgesStates();
    this.GraphPanelRendererService.changeEdgesPaintStyles(this.workflow);
  }

  updateAndRerenderEdges(data) {
    this.WorkflowService.updateTypeKnowledge(data.knowledge);
    if (this.selectedNode) {
      this.GraphNodesService.getNodeParameters(this.selectedNode).then((node, mode) => {
        if (mode === 'sync') {
          this.$scope.$digest();
        }
      });
    }
    this.rerenderEdges();
  }

  getWorkflow() {
    return this.workflow;
  }

  getPredefColors() {
    return this.WorkflowService.getPredefColors();
  }

  getSelectedNode() {
    return this.selectedNode;
  }

  unselectNode() {
    if (this.selectedNode) {
      this.MultiSelectionService.removeNodesFromSelection([this.selectedNode.id]);
      this.selectedNode = null;
    }
  }

}

exports.inject = function(module) {
  module.controller('WorkflowsEditorController', WorkflowsEditorController);
};
