/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

class WorkflowsEditorController {

  /* @ngInject */
  constructor(workflowWithResults, $scope, $state, $rootScope, WorkflowCloneService,
              Report, MultiSelectionService, WorkflowService, DatasourcesPanelService,
              ConfirmationModalService, ExportModalService, GraphNodesService, NotificationService,
              ServerCommunication, CopyPasteService, BottomBarService, NodeCopyPasteVisitorService,
              EventsService, WorkflowsEditorService, AdapterService, MouseEvent) {

    _.assign(this, {
      workflowWithResults, $scope, $state, $rootScope, WorkflowCloneService,
      Report, MultiSelectionService, WorkflowService, DatasourcesPanelService,
      ConfirmationModalService, ExportModalService, GraphNodesService, NotificationService,
      ServerCommunication, CopyPasteService, BottomBarService, NodeCopyPasteVisitorService,
      EventsService, WorkflowsEditorService, AdapterService, MouseEvent
    });

    WorkflowService.initRootWorkflow(workflowWithResults);

    $rootScope.$watch(() => this.WorkflowService.getCurrentWorkflow().name, (newValue) => {
      $rootScope.pageTitle = newValue;
    });

    $scope.$watch(() => this.MultiSelectionService.getSelectedNodeIds(), (newValue) => {
      if (newValue.length === 0) {
        this.selectedNode = null;
      }
    });

    $scope.$watch(() => this.DatasourcesPanelService.isDatasourcesOpened, (newValue) => {
      this.isDatasourcesOpened = newValue;
    });

    this.AdapterService.setMouseClickOnPortFunction((data) => {
      this.openReport(data);
    });

    this.BottomBarData = BottomBarService.tabsState;
    this.selectedNode = null;
    this._editableModeEventListeners = [];
    this.init(workflowWithResults);
  }

  closeDatasources() {
    this.DatasourcesPanelService.closeDatasources();
  }

  _loadReports(data) {
    const report = data.resultEntities;
    if (!_.isEmpty(report)) {
      this.WorkflowService.getCurrentWorkflow().setPortTypesFromReport(report);
      this.Report.createReportEntities(report.id, report);
      this._initReportListeners();
    }
  }

  openReport(data) {
    const workflow = this.WorkflowService.getCurrentWorkflow();
    const node = workflow.getNodeById(data.port.nodeId);
    this.selectedPortObject = {
      portIdx: data.port.index,
      node: node
    };

    const reportEntityId = node.getResult(data.reference.getParameter('portIndex'));

    this.loadReportById(reportEntityId);
    this.Report.openReport();
  }

  _initReportListeners() {
    if (this.inited) {
      return false;
    }

    this.inited = true;
  }

  init(workflowWithResults) {
    this.WorkflowService.getCurrentWorkflow().updateState(workflowWithResults.executionReport);
    this.initListeners();
    if (this.WorkflowService.isWorkflowRunning()) {
      this._setRunningMode();
    }
    this._loadReports(workflowWithResults.executionReport);
  }

  initListeners() {
    this.$scope.$watch(() => this.getWorkflow(), () => {
      // So attributes panel does not show attributes from previous workflow node.
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
      if (this.WorkflowService.isWorkflowRunning()) {
        this._setRunningMode();
      } else {
        this._setEditableMode();
      }
      this.AdapterService.render();
    });

    this.$scope.$on('ServerCommunication.MESSAGE.inferredState', (event, data) => {
      const currentWorkflow = this.WorkflowService.getCurrentWorkflow();
      if (data.id === currentWorkflow.id) {
        this.updateAndRerenderEdges(data);
        if (data.states) {
          this.WorkflowService.onInferredState(data.states);
          if (this.WorkflowService.isWorkflowRunning()) {
            this._setRunningMode();
          } else {
            this._setEditableMode();
          }
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
      const isModKeyDown = this.MouseEvent.isModKeyDown(data.originalEvent);

      if (!isModKeyDown) {
        this.selectedNode = data.selectedNode;
        this.loadParametersForNode();
      } else if (isModKeyDown && this.selectedNode && this.selectedNode.id === data.selectedNode.id) {
        this.unselectNode();
      }
    });

    this.$scope.$on('StatusBar.CLONE_WORKFLOW', () => {
      const currentWorkflow = this.WorkflowService.getCurrentWorkflow();
      this.WorkflowCloneService.openModal(this._goToWorkflow.bind(this), currentWorkflow);
    });

    this.$scope.$on('StatusBar.EXPORT_CLICK', () => {
      this.ExportModalService.showModal();
    });

    this._reinitEditableModeListeners();
  }

  _reinitEditableModeListeners() {
    this._unbindEditorListeners();
    this._editableModeEventListeners = [
      this.$scope.$on('Edge.CREATE', (data, args) => {
        const workflow = this.getWorkflow();
        workflow.addEdge(args.edge);
        this.WorkflowService.updateEdgesStates();
      }),

      this.$scope.$on('Edge.REMOVE', (data, args) => {
        this.getWorkflow().removeEdge(args.edge);
      }),

      this.$scope.$on('Keyboard.KEY_PRESSED_DEL', () => {
        if (this.MultiSelectionService.getSelectedNodeIds().length > 0) {
          this.WorkflowsEditorService.handleDelete();
        }
      }),
      this.EventsService.on(this.EventsService.EVENTS.WORKFLOW_DELETE_SELECTED_ELEMENT, this._handleDelete.bind(this)),

      this.$scope.$on('$destroy', () => {
        this.AdapterService.reset();
        this.NotificationService.clearToasts();
        this._unbindEditorListeners();
      })
    ];
  }

  _goToWorkflow(workflow) {
    const id = workflow.workflowId;
    this.ServerCommunication.unsubscribeFromAllExchanges();
    this.$state.go(this.$state.current, {id: id}, {reload: true});
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

  updateAndRerenderEdges(data) {
    this.WorkflowService.updateTypeKnowledge(data.knowledge);
    if (this.selectedNode) {
      this.GraphNodesService.getNodeParameters(this.selectedNode).then((node, mode) => {
        if (mode === 'sync') {
          this.$scope.$digest();
        }
      });
    }
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

  getSelectedNode() {
    return this.selectedNode;
  }

  unselectNode() {
    if (this.selectedNode) {
      this.MultiSelectionService.removeNodeIdsFromSelection([this.selectedNode.id]);
      this.selectedNode = null;
    }
  }

  isEditable() {
    return this.WorkflowService.isWorkflowEditable();
  }

  _handleDelete() {
    if (!this.WorkflowService.isWorkflowEditable()) {
      this.$log.log('WorkflowsEditorController', 'Cannot remove nodes if not editable');
      return;
    }

    let selectedNodeIds = this.MultiSelectionService.getSelectedNodeIds();
    let sinkOrSourceNodeIds = _.filter(selectedNodeIds, (nodeId) => {
      let node = this.getWorkflow().getNodeById(nodeId);
      return this.GraphNodesService.isSinkOrSource(node);
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
    this.AdapterService.removeNodes(nodeIdsToBeRemoved);
  }
}

exports.inject = function (module) {
  module.controller('WorkflowsEditorController', WorkflowsEditorController);
};
