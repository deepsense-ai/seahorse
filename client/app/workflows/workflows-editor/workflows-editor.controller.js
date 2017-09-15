'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */

import NodeCopyPasteVisitor from './node-copy-paste-visitor.js';

class WorkflowsEditorController {

  /* @ngInject */
  constructor(workflowWithResults, $scope, $state, $q, $rootScope, $log,
    GraphNode, Edge, config, Report, MultiSelectionService, PageService, Operations, GraphPanelRendererService,
    WorkflowService, MouseEvent, ConfirmationModalService, ExportModalService, GraphNodesService, NotificationService,
    ServerCommunication, CopyPasteService, SideBarService, BottomBarService, WorkflowStatusBarService) {

    WorkflowService.initRootWorkflow(workflowWithResults);

    _.assign(this, {
      $scope, $state, $q, $rootScope, $log,
      GraphNode, Edge, config, Report, MultiSelectionService, PageService, Operations, GraphPanelRendererService,
      WorkflowService, MouseEvent, ConfirmationModalService, ExportModalService, GraphNodesService, NotificationService,
      ServerCommunication, CopyPasteService, SideBarService, BottomBarService, WorkflowStatusBarService
    });

    this.BottomBarData = BottomBarService.tabsState;
    this.SideBarData = SideBarService.data;
    this.selectedNode = null;
    this.catalog = Operations.getCatalog();
    this.isReportMode = false;
    this.eventListeners = [];
    this.zoomId = 'flowchart-box';
    this.nodeCopyPasteVisitor = new NodeCopyPasteVisitor(MultiSelectionService, $q,
      $scope, WorkflowService, this, GraphNodesService);

    this.init(workflowWithResults);
  }

  _loadReports(data) {
    let report = data.resultEntities;
    if (!_.isEmpty(report)) {
      this.WorkflowService.getCurrentWorkflow().setPortTypesFromReport(report);
      this.Report.createReportEntities(report.id, report);
      this._initReportListeners();
      this.$scope.$applyAsync(() => {
        this.GraphPanelRendererService.rerender(this.getWorkflow());
      });
    }
  }

  _initReportListeners() {
    if (this.inited) {
      return false;
    }

    this.$scope.$on('OutputPort.LEFT_CLICK', (event, data) => {
      let workflow = this.WorkflowService.getCurrentWorkflow();
      let node = workflow.getNodeById(data.portObject.nodeId);
      this.selectedPortObject = {
        portIdx: data.portObject.index,
        node: node
      };
      this.MultiSelectionService.clearSelection();
      this.MultiSelectionService.addNodeIdsToSelection([node.id]);
      this.workflowIdForReport = workflow.id;
      this.nodeIdForReport = node.id;
      this.selectedNode = node;
      this.loadParametersForNode();

      let reportEntityId = node.getResult(data.reference.getParameter('portIndex'));
      this.loadReportById(reportEntityId);
    });

    this.inited = true;
  }

  init(workflowWithResults) {
    this.PageService.setTitle('Workflow editor');
    this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
    this.GraphPanelRendererService.setZoom(1.0);
    this.CopyPasteService.registerCopyPasteVisitor(this.nodeCopyPasteVisitor);
    this.WorkflowService.getCurrentWorkflow().updateState(workflowWithResults.executionReport);
    this.initListeners();
    this._loadReports(workflowWithResults.executionReport);
  }

  initListeners() {
    this.$scope.$on('ServerCommunication.MESSAGE.ready', (event, data) => {
      this.$log.debug('Received a Ready message from Session Executor. Reconnecting.');
      this.ServerCommunication.reconnect();
    });

    // So attributes panel does not show attributes from previous workflow node.
    this.$scope.$watch(() => this.getWorkflow(), () => {
      this.unselectNode();
    });

    this.$scope.$on('ServerCommunication.MESSAGE.executionStatus', (event, data) => {
      this.getWorkflow().updateState(data);
      this._loadReports(data);

      if (this.selectedPortObject) {
        this.report = null;
        let reportEntityId = this.selectedPortObject.node.state.results[this.selectedPortObject.portIdx];
        this.loadReportById(reportEntityId);
      }
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
        this.getWorkflow().updateState(data.states);
      }
    });

    this.$scope.$on('StatusBar.RUN', () => {
      this.unbindListeners();
      this.isReportMode = true;
      this.isRunning = true;
      this.CopyPasteService.setEnabled(false);
      let nodesToExecute = this.MultiSelectionService.getSelectedNodeIds();
      this.ServerCommunication.sendLaunchToWorkflowExchange(nodesToExecute);
    });

    this.$scope.$on('AttributePanel.UNSELECT_NODE', () => {
      this.unselectNode();
      this.$scope.$digest();
    });

    this.$scope.$on('OpenReportTab.SELECT_NODE', () => {
      if (this.workflowIdForReport && this.nodeIdForReport) {
        let workflow = this.getWorkflow();
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
        this.getWorkflow().addEdge(args.edge);
      }),

      this.$scope.$on(this.Edge.REMOVE, (data, args) => {
        this.getWorkflow().removeEdge(args.edge);
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
        let node = this.GraphNodesService.createNodeAndAdd(this.getWorkflow(), {
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
          this.GraphPanelRendererService.rerender(this.getWorkflow());
        });
      }),

      this.$scope.$on('StatusBar.EXPORT_CLICK', () => {
        this.ExportModalService.showModal();
      }),

      this.$scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
        let selectedNodeIds = this.MultiSelectionService.getSelectedNodeIds();
        let sinkOrSourceNodeIds = _.filter(selectedNodeIds, (nodeId) => {
          let node = this.getWorkflow().getNodeById(nodeId);
          return this._isSinkOrSource(node)
        });
        if (sinkOrSourceNodeIds.length > 0) {
          let msg = "Cannot delete source nor sink nodes"
          this.NotificationService.showError({
            title: "Illegal node deletion",
            message: msg
          }, msg);
        }
        let nodeIdsToBeRemoved = _.difference(selectedNodeIds, sinkOrSourceNodeIds);
        this.getWorkflow().removeNodes(nodeIdsToBeRemoved);
        this.GraphPanelRendererService.removeNodes(nodeIdsToBeRemoved);
        this.MultiSelectionService.clearSelection();
        this.unselectNode();
        this.$scope.$apply();
        this.selectedPortObject = null;
      }),

      this.$scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          this.$scope.$applyAsync(() => {
            this.GraphPanelRendererService.rerender(this.getWorkflow());
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
    this.GraphPanelRendererService.changeEdgesPaintStyles(this.getWorkflow());
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

  loadReportById(reportEntityId) {
    if (this.Report.hasReportEntity(reportEntityId)) {
      this.Report.getReport(reportEntityId).then(report => {
        this.report = report;
        this.Report.openReport();
      });
    }
  }

  getWorkflow() {
    return this.WorkflowService.getCurrentWorkflow();
  }

  getPredefColors() {
    return this.WorkflowService.getCurrentWorkflow().predefColors;
  }

  getSelectedNode() {
    return this.selectedNode;
  }

  unselectNode() {
    if (this.selectedNode) {
      this.MultiSelectionService.removeNodeIdsFromSelection([this.selectedNode.id]);
      this.selectedNode = null;
      this.selectedNodeBeforeRun = null;
    }
  }

  _isSinkOrSource(node) {
    let sourceId = 'f94b04d7-ec34-42f7-8100-93fe235c89f8';
    let sinkId = 'e652238f-7415-4da6-95c6-ee33808561b2';
    return node.operationId === sourceId || node.operationId === sinkId;
  }

}

exports.inject = function(module) {
  module.controller('WorkflowsEditorController', WorkflowsEditorController);
};
