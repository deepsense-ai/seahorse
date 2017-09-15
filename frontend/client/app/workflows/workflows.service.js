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

import {specialOperations} from 'APP/enums/special-operations.js';
import {sessionStatus} from 'APP/enums/session-status.js';

/* @ngInject */
function WorkflowService($rootScope, $log, Workflow, OperationsHierarchyService, WorkflowsApiClient, Operations,
                         ConfirmationModalService, DefaultInnerWorkflowGenerator, debounce, SessionManagerApi,
                         SessionManager, ServerCommunication, UserService, DeepsenseCycleAnalyser) {

  const INNER_WORKFLOW_PARAM_NAME = 'inner workflow';

  class WorkflowServiceClass {

    constructor() {
      this._workflowsStack = [];
      this._innerWorkflowByNodeId = {};
      // We want to save workflow after all intermediate changes are resolved. Intermediate state might be invalid.
      // Debounce takes care of that and additionally reduces unnecessary client-server communication.
      // Example:
      // Inner workflow has nodes and publicParam list. Public params point at specific nodes.
      // Let's say we are removing node with public params. This change gets propagated to publicParam list next
      // digest cycle. For one digest cycle state is invalid - public params list points non-existing node.
      this._saveWorkflow = debounce(200, (newSerializedWorkflow, oldSerializedWorkflow) => {
        if(newSerializedWorkflow !== oldSerializedWorkflow) {
          $log.log('Saving workflow after change...', newSerializedWorkflow);
          WorkflowsApiClient.updateWorkflow(newSerializedWorkflow);
        }
      });

      // All rootScope listeners must go to initRootWorkflow method.
      // Otherwise those would get lost upon cloning workflow.
    }

    initRootWorkflow(workflowData) {
      this._workflowsStack = [];
      let workflow = this._deserializeWorkflow(workflowData);
      workflow.workflowType = 'root';
      workflow.workflowStatus = 'editor';
      workflow.sessionStatus = sessionStatus.NOT_RUNNING;

      workflow.owner = {
        id: workflowData.workflowInfo.ownerId,
        name: workflowData.workflowInfo.ownerName
      };

      this._initializeAllInnerWorkflows(workflow, workflow);

      $rootScope.$watch(() => workflow.serialize(), this._saveWorkflow, true);
      $rootScope.$watch(() => SessionManager.statusForWorkflowId(workflow.id), (newStatus) => {
        workflow.sessionStatus = newStatus;
      });

      $rootScope.$on('StatusBar.START_EDITING', () => {
        const workflow = this.getRootWorkflow();
        SessionManagerApi.startSession({
          workflowId: workflow.id,
          cluster: workflow.cluster
        });
      });

      $rootScope.$on('StatusBar.STOP_EDITING', (event, force = false) => {
        if (force) {
          this.stopEditing();
        } else {
          ConfirmationModalService.showModal({
            message: 'Are you sure you want to stop executor? Cached results will disappear.'
          }).then(() => this.stopEditing());
        }
      });

      $rootScope.$on('AttributesPanel.OPEN_INNER_WORKFLOW', (event, data) => {
        let workflow = this._innerWorkflowByNodeId[data.nodeId];
        this._workflowsStack.push(workflow);
      });

      $rootScope.$on('StatusBar.CLOSE-INNER-WORKFLOW', () => {
        this._workflowsStack.pop();
      });

      this._watchForNewCustomTransformers(workflow, workflow);
      this._workflowsStack.push(workflow);
      this.fetchCluster();

      const unregisterSynchronization = $rootScope.$on('ServerCommunication.MESSAGE.heartbeat', (event, data) => {
        if (data.workflowId === workflow.id) {
          $log.log('Received first hearbeat. Synchronizing with executor...');
          ServerCommunication.sendSynchronize();
          unregisterSynchronization();
        }
      });
    }

    // TODO Add enums for workflowType, workflowStatus
    initInnerWorkflow(node, rootWorkflow) {
      let innerWorkflowData = node.parametersValues[INNER_WORKFLOW_PARAM_NAME];
      let innerWorkflow = this._deserializeInnerWorkflow(innerWorkflowData);
      innerWorkflow.workflowType = 'inner';
      innerWorkflow.workflowStatus = 'editor';
      innerWorkflow.sessionStatus = sessionStatus.NOT_RUNNING;
      innerWorkflow.owner = rootWorkflow.owner;

      innerWorkflow.publicParams = innerWorkflow.publicParams || [];
      this._innerWorkflowByNodeId[node.id] = innerWorkflow;

      $rootScope.$watch(() => SessionManager.statusForWorkflowId(rootWorkflow.id), (newStatus) => {
        innerWorkflow.sessionStatus = newStatus;
      });

      $rootScope.$watch(() => this._serializeInnerWorkflow(innerWorkflow), (newVal) => {
        node.parametersValues = node.parametersValues || {};
        node.parametersValues[INNER_WORKFLOW_PARAM_NAME] = newVal;
      }, true);

      this._watchForNewCustomTransformers(innerWorkflow, rootWorkflow);
      this._initializeAllInnerWorkflows(innerWorkflow, rootWorkflow);
    }

    _initializeAllInnerWorkflows(workflow, rootWorkflow) {
      const nodes = _.values(workflow.getNodes());
      nodes.filter((n) => n.operationId === specialOperations.CUSTOM_TRANSFORMER.NODE)
        .forEach((node) => this.initInnerWorkflow(node, rootWorkflow));
    }

    _watchForNewCustomTransformers(workflow, rootWorkflow) {
      $rootScope.$watchCollection(() => workflow.getNodes(), (newNodes, oldNodes) => {
        let addedNodeIds = _.difference(_.keys(newNodes), _.keys(oldNodes));
        let addedNodes = _.map(addedNodeIds, nodeId => newNodes[nodeId]);
        let addedCustomWorkflowNodes = _.filter(addedNodes,
          (n) => n.operationId === specialOperations.CUSTOM_TRANSFORMER.NODE
        );
        _.forEach(addedCustomWorkflowNodes, (addedNode) => {
          if (_.isUndefined(addedNode.parametersValues[INNER_WORKFLOW_PARAM_NAME])) {
            addedNode.parametersValues[INNER_WORKFLOW_PARAM_NAME] = DefaultInnerWorkflowGenerator.create();
          }
          this.initInnerWorkflow(addedNode, rootWorkflow);
        });
      });
    }

    _getRootWorkflowsWithInnerWorkflows() {
      const innerWorkflows = _.values(this._innerWorkflowByNodeId);
      return [this.getRootWorkflow()].concat(innerWorkflows);
    }

    _serializeInnerWorkflow(workflow) {
      let workflowData = workflow.serialize();
      workflowData.publicParams = workflow.publicParams;
      return workflowData;
    }

    _deserializeInnerWorkflow(workflowData) {
      let workflow = this._deserializeWorkflow(workflowData);
      workflow.publicParams = workflowData.publicParams || [];
      return workflow;
    }

    _deserializeWorkflow(workflowData) {
      let operations = Operations.getData();
      let workflow = new Workflow();
      let thirdPartyData = workflowData.thirdPartyData || {};
      workflow.id = workflowData.id;
      workflow.name = (thirdPartyData.gui || {}).name;
      workflow.description = (thirdPartyData.gui || {}).description;
      workflow.createNodes(workflowData.workflow.nodes, operations, workflowData.thirdPartyData);
      workflow.createEdges(workflowData.workflow.connections);
      workflow.updateEdgesStates(OperationsHierarchyService);
      return workflow;
    }

    isWorkflowEditable() {
      const workflow = this.getCurrentWorkflow();
      return workflow.workflowStatus === 'editor' &&
        workflow.sessionStatus === sessionStatus.RUNNING &&
        this.isCurrentUserOwnerOfCurrentWorkflow();
    }

    isWorkflowRunning() {
      let statuses = _.chain(this.getCurrentWorkflow().getNodes())
        .map((node) => {
          return node.state && node.state.status;
        })
        .value();
      let idx = _.findIndex(statuses, (status) => {
        return status === 'status_queued' || status === 'status_running';
      });

      return idx !== -1;
    }

    isExecutorForCurrentWorkflowRunning() {
      const status = this.getCurrentWorkflow().sessionStatus;
      return status === sessionStatus.RUNNING || status === sessionStatus.CREATING;
    }

    getCurrentWorkflow() {
      return _.last(this._workflowsStack);
    }

    onInferredState(states) {
      this._getRootWorkflowsWithInnerWorkflows().forEach(w => w.updateState(states));
    }

    clearGraph() {
      this.getCurrentWorkflow().clearGraph();
    }

    updateTypeKnowledge(knowledge) {
      this._getRootWorkflowsWithInnerWorkflows().forEach(w => w.updateTypeKnowledge(knowledge));
    }

    updateEdgesStates() {
      this.getCurrentWorkflow().updateEdgesStates(OperationsHierarchyService);
    }

    getRootWorkflow() {
      return this._workflowsStack[0];
    }

    getAllWorkflows() {
      return this._workflowsData;
    }

    removeWorkflowFromList(workflowId) {
      let foundWorkflow = this._workflowsData.find((workflow) => workflow.id === workflowId);
      let workflowIndex = this._workflowsData.indexOf(foundWorkflow);
      if (workflowIndex >= 0) {
        this._workflowsData.splice(workflowIndex, 1);
      }
    }

    deleteWorkflow(workflowId) {
      WorkflowsApiClient.deleteWorkflow(workflowId).then(() => {
        this.removeWorkflowFromList(workflowId);
      });
    }

    downloadWorkflow(workflowId) {
      return WorkflowsApiClient.getWorkflow(workflowId);
    }

    downloadWorkflows() {
      return WorkflowsApiClient.getAllWorkflows().then((workflows) => {
        this._workflowsData = workflows; // TODO There should be no state here. Get rid of it
        return workflows;
      });
    }

    stopEditing() {
      const workflow = this.getRootWorkflow();
      SessionManagerApi.deleteSessionById(workflow.id);
    }

    bindPresetToCurrentWorkflow(presetId) {
      const rootWorkflow = this.getRootWorkflow();
      return WorkflowsApiClient.bindPresetToWorkflow(presetId, rootWorkflow.id)
        .then(() => this.fetchCluster());
    }

    fetchCluster(workflow) {
      workflow = workflow || this.getRootWorkflow();

      if (workflow) {
        return WorkflowsApiClient.getPresetByWorkflowId(workflow.id)
          .then((result) => {
            workflow.cluster = result;
          })
          .catch((error) => {
            $log.error('Cluster information is not available for workflow!', error);
          });
      } else {
        return false;
      }
    }

    canAddNewConnection(connection) {
      return !this.doesCycleExist() && this.isConnectionValid(connection);
    }

    doesCycleExist() {
      const workflow = this.getCurrentWorkflow();
      return DeepsenseCycleAnalyser.cycleExists(workflow); //TODO move component's function cycleExists here
    }

    isConnectionValid(connection) {
      const workflow = this.getCurrentWorkflow();

      const startNode = workflow.getNodeById(connection.startNodeId);
      const startNodeTypeQualifier = startNode.originalOutput[connection.startPortId].typeQualifier[0];

      const endNode = workflow.getNodeById(connection.endNodeId);
      const endNodeTypeQualifier = endNode.input[connection.endPortId].typeQualifier[0];

      return OperationsHierarchyService.IsDescendantOf(startNodeTypeQualifier, [endNodeTypeQualifier]);
    }

    isCurrentUserOwnerOfCurrentWorkflow() {
      const workflow = this.getCurrentWorkflow();
      return workflow.owner.id === UserService.getSeahorseUser().id;
    }

  }

  return new WorkflowServiceClass();
}

exports.function = WorkflowService;

exports.inject = function (module) {
  module.factory('WorkflowService', WorkflowService);
};
