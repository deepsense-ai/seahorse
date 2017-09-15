'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */

class WorkflowsEditorController {

  /* @ngInject */
  constructor(workflowWithResults, $scope, $state, $q, $rootScope, $log, $timeout, specialOperations,
    GraphNode, Edge, config, Report, MultiSelectionService, PageService, Operations, GraphPanelRendererService,
    WorkflowService, MouseEvent, ConfirmationModalService, ExportModalService, GraphNodesService, NotificationService,
    ServerCommunication, CopyPasteService, SideBarService, BottomBarService, NodeCopyPasteVisitorService) {

    WorkflowService.initRootWorkflow(workflowWithResults);

    _.assign(this, {
      $scope, $state, $q, $rootScope, $log, $timeout, specialOperations,
      GraphNode, Edge, config, Report, MultiSelectionService, PageService, Operations, GraphPanelRendererService,
      WorkflowService, MouseEvent, ConfirmationModalService, ExportModalService, GraphNodesService, NotificationService,
      ServerCommunication, CopyPasteService, SideBarService, BottomBarService, NodeCopyPasteVisitorService
    });

    this.BottomBarData = BottomBarService.tabsState;
    this.SideBarData = SideBarService.data;
    this.selectedNode = null;
    this.catalog = Operations.getCatalog();
    this._editableModeEventListeners = [];
    this.zoomId = 'flowchart-box';
    this.init(workflowWithResults);
  }

  _loadReports(data) {
    let report = data.resultEntities;
    if (!_.isEmpty(report)) {
      this.WorkflowService.getCurrentWorkflow().setPortTypesFromReport(report);
      this.Report.createReportEntities(report.id, report);
      this._initReportListeners();
      this.$scope.$applyAsync(() => {
        this.GraphPanelRendererService.rerender(this.getWorkflow(), this.selectedOutputPort);
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
      this.selectedOutputPort = data.portObject.id;

      let reportEntityId = node.getResult(data.reference.getParameter('portIndex'));
      this.loadReportById(reportEntityId);
      this.Report.openReport();
      this.GraphPanelRendererService.rerender(this.getWorkflow(), this.selectedOutputPort);
    });

    this.inited = true;
  }

  init(workflowWithResults) {
    this.PageService.setTitle('Workflow editor');
    this.GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.EDITOR_RENDER_MODE);
    this.GraphPanelRendererService.setZoom(1.0);
    this.WorkflowService.getCurrentWorkflow().updateState(workflowWithResults.executionReport);
    this.initListeners();
    if (this.WorkflowService.isWorkflowRunning()) {
      this._setRunningMode();
    }
    this._loadReports(workflowWithResults.executionReport);
  }

  initListeners() {
    this.$scope.$on('ServerCommunication.MESSAGE.ready', (event, ready) => {
      this.$log.debug('Received a Ready message from Session Executor. Reconnecting.');

      // workflowId being null means that entire Session Executor has been restarted
      if (!ready.workflowId || ready.workflowId === this.WorkflowService.getRootWorkflow().id) {
        this.ServerCommunication.reconnect();
        this.NotificationService.showWithParams({
          notificationType: ready.content.msgType,
          message: ready.content.text
        });

        this._setEditableMode();
      }
    });

    this.$scope.$watch(() => this.getWorkflow(), () => {
      // So attributes panel does not show attributes from previous workflow node.
      this.unselectNode();

      // HACK. Further down in digest cycle there are changes in stuff that navigation service relies on (probably node-related)
      // Timeout allows us to broadcast fit event after other components.
      this.$timeout(() => {
        this.$rootScope.$broadcast('INTERACTION-PANEL.FIT', {
          zoomId: this.zoomId
        });
      }, 10);
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
        this._setEditableMode();
      }
    });

    this.$scope.$on('ServerCommunication.MESSAGE.inferredState', (event, data) => {
      this.updateAndRerenderEdges(data);
      if (data.states) {
        this.WorkflowService.onInferredState(data.states);
        if (!this.WorkflowService.isWorkflowRunning()) {
          this._setEditableMode();
        }
      }
    });

    this.$scope.$on('StatusBar.RUN', () => {
      this._setRunningMode();
      let nodesToExecute = this.MultiSelectionService.getSelectedNodeIds();
      this.ServerCommunication.sendLaunchToWorkflowExchange(nodesToExecute);
    });

    this.$scope.$on('AttributePanel.UNSELECT_NODE', () => {
      this.selectedNode = null;
      this.$scope.$digest();
    });

    this.$scope.$on('StatusBar.ABORT', () => {
      this.WorkflowService.getCurrentWorkflow().workflowStatus = 'aborting';
      this.ServerCommunication.sendAbortToWorkflowExchange();
    });

    this.$scope.$on('GraphNode.CLICK', (event, data) => {
      if (!data.originalEvent.ctrlKey) {
        this.selectedNode = data.selectedNode;
        this.loadParametersForNode();
      } else if (data.originalEvent.ctrlKey && this.selectedNode && this.selectedNode.id === data.selectedNode.id) {
        this.unselectNode();
      }
    });

    this._reinitEditableModeListeners();
  }

  _reinitEditableModeListeners() {
    this._unbindEditorListeners();
    this._editableModeEventListeners = [
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
        let elementOffsetX = _.has(this.specialOperations, args.elementId) ? 40 : 100;
        let elementOffsetY = 30;
        this.GraphNodesService.createNodeAndAdd(this.getWorkflow(), {
          operation: operation,
          // TODO check if we reached right and bottom end of flowchart box,
          x: positionX > elementOffsetX ? positionX - elementOffsetX : 0,
          y: positionY > elementOffsetY ? positionY - elementOffsetY : 0
        });
        // Call of $apply assures that a DOM element for the node is added before method exit.
        // It prevents exceptions in browser's console.
        this.$rootScope.$apply();
      }),

      this.$scope.$on('StatusBar.CLEAR_CLICK', () => {
        this.ConfirmationModalService.showModal({
          message: 'The operation clears the whole workflow graph and it cannot be undone afterwards.'
        }).
        then(() => {
          this.WorkflowService.clearGraph();
          this.GraphPanelRendererService.rerender(this.getWorkflow(), this.selectedOutputPort);
        });
      }),

      this.$scope.$on('StatusBar.EXPORT_CLICK', () => {
        this.ExportModalService.showModal();
      }),

      this.$scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
        let selectedNodeIds = this.MultiSelectionService.getSelectedNodeIds();
        let sinkOrSourceNodeIds = _.filter(selectedNodeIds, (nodeId) => {
          let node = this.getWorkflow().getNodeById(nodeId);
          return this._isSinkOrSource(node);
        });
        if (sinkOrSourceNodeIds.length > 0) {
          let msg = 'Cannot delete source nor sink nodes';
          this.NotificationService.showError({
            title: 'Illegal node deletion',
            message: msg
          }, msg);
        }
        let nodeIdsToBeRemoved = _.difference(selectedNodeIds, sinkOrSourceNodeIds);
        this.getWorkflow().removeNodes(nodeIdsToBeRemoved);
        this.MultiSelectionService.clearSelection();
        this.unselectNode();
        this.selectedPortObject = null;
        this.GraphPanelRendererService.removeNodes(nodeIdsToBeRemoved);
      }),

      this.$scope.$watchCollection('workflow.getWorkflow().getNodesIds()', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          this.$scope.$applyAsync(() => {
            this.GraphPanelRendererService.rerender(this.getWorkflow(), this.selectedOutputPort);
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
      }),

      this.$scope.$on('AttributesPanel.OPEN_INNER_WORKFLOW', (event, data) => {
        let workflow = this.WorkflowService._innerWorkflowByNodeId[data.nodeId];
        this.WorkflowService._workflowsStack.push(workflow);
      }),

      this.$scope.$on('StatusBar.CLOSE-INNER-WORKFLOW', () => {
        this.WorkflowService._workflowsStack.pop();
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

  _setRunningMode() {
    this._unbindEditorListeners();
    this.WorkflowService.getCurrentWorkflow().workflowStatus = 'running';
    this.CopyPasteService.setEnabled(false);
    // This event and WorkflowEditor.EDITOR_MODE_SET are used here ONLY for toggle directive, because its
    // driven by events. toggle directive should probably accept boolean argument. In that case we would
    // simply pass isRunning property there and would get rid of those two events.
    this.$rootScope.$broadcast('WorkflowEditor.RUNNING_MODE_SET');
  }

  _setEditableMode() {
    this._reinitEditableModeListeners();
    this.WorkflowService.getCurrentWorkflow().workflowStatus = 'editor';
    this.CopyPasteService.setEnabled(true);
    this.$rootScope.$broadcast('WorkflowEditor.EDITOR_MODE_SET');
  }

  _unbindEditorListeners() {
    this._editableModeEventListeners.forEach(func => func());
    this._editableModeEventListeners = [];
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
