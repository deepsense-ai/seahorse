'use strict';

/* @ngInject */
function WorkflowService(Workflow, OperationsHierarchyService, WorkflowsApiClient, Operations, $rootScope,
  DefaultInnerWorkflowGenerator) {

  // TODO Disable clean/export/run in inner workflows
  // TODO Prevent deleting Sink and Source

  const CUSTOM_TRANSFORMER_ID = '65240399-2987-41bd-ba7e-2944d60a3404';
  const INNER_WORKFLOW_PARAM_NAME = 'inner workflow';

  class WorkflowServiceClass {

    constructor() {
      this._workflowsStack = [];
      this._innerWorkflowByNodeId = {};
      this.init();
    }

    init() {
      WorkflowsApiClient.getAllWorkflows().then((data) => {
        this._workflowsData = data;
      });

      $rootScope.$on('AttributesPanel.OPEN_INNER_WORKFLOW', (event, {
        nodeId
      }) => {
        let workflow = this._innerWorkflowByNodeId[nodeId];
        this._workflowsStack.push(workflow);
      });

      $rootScope.$on('StatusBar.CLOSE-INNER-WORKFLOW', () => {
        this._workflowsStack.pop();
      });
    }

    initRootWorkflow(workflowData) {
      let workflow = this._deserializeWorkflow(workflowData);
      workflow.workflowType = 'root';
      workflow.isRunning = false;

      let nodes = _.values(workflow.getNodes());
      nodes.filter((n) => n.operationId === CUSTOM_TRANSFORMER_ID)
        .forEach((node) => this.initInnerWorkflow(node));

      $rootScope.$watch(() => workflow.serialize(), (newSerializedWorkflow) => {
        console.log('Saving workflow after change...', newSerializedWorkflow);
        WorkflowsApiClient.updateWorkflow(newSerializedWorkflow);
      }, true);

      this._watchForNewCustomTransformers(workflow);

      this._workflowsStack.push(workflow);
    }

    initInnerWorkflow(node) {
      let innerWorkflowData = node.parametersValues[INNER_WORKFLOW_PARAM_NAME];
      let innerWorkflow = this._deserializeInnerWorkflow(innerWorkflowData);
      innerWorkflow.workflowType = 'inner';
      this._innerWorkflowByNodeId[node.id] = innerWorkflow;

      let nestedCustomTransformerNodes = _.filter(_.values(innerWorkflow.getNodes()), (n) => n.operationId === CUSTOM_TRANSFORMER_ID);
      _.forEach(nestedCustomTransformerNodes, (node) => this.initInnerWorkflow(node));

      $rootScope.$watch(() => this._serializeInnerWorkflow(innerWorkflow), (newVal) => {
        node.parametersValues = node.parametersValues || {};
        node.parametersValues[INNER_WORKFLOW_PARAM_NAME] = newVal;
      }, true);

      this._watchForNewCustomTransformers(innerWorkflow);
    }

    _watchForNewCustomTransformers(workflow) {
      $rootScope.$watchCollection(() => workflow.getNodes(), (newNodes, oldNodes) => {
        let addedNodeIds = _.difference(_.keys(newNodes), _.keys(oldNodes));
        let addedNodes = _.map(addedNodeIds, nodeId => newNodes[nodeId]);
        let addedCustomWorkflowNodes = _.filter(addedNodes, (n) => n.operationId === CUSTOM_TRANSFORMER_ID);
        _.forEach(addedCustomWorkflowNodes, (addedNode) => {
          if (_.isUndefined(addedNode.parametersValues[INNER_WORKFLOW_PARAM_NAME])) {
            addedNode.parametersValues[INNER_WORKFLOW_PARAM_NAME] = DefaultInnerWorkflowGenerator.create();
          }
          this.initInnerWorkflow(addedNode);
        });
      });
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
      workflow.predefColors = (thirdPartyData.gui || {}).predefColors || workflow.predefColors;
      workflow.createNodes(workflowData.workflow.nodes, operations, workflowData.thirdPartyData);
      workflow.createEdges(workflowData.workflow.connections);
      workflow.updateEdgesStates(OperationsHierarchyService);
      return workflow;
    }

    isWorkflowRunning() {
      let statuses = _.chain(this.getCurrentWorkflow().getNodes())
        .map((node) => {
          return node.state;
        })
        .map((state) => {
          return state.status;
        })
        .value();
      let idx = _.findIndex(statuses, (status) => {
        return status === 'status_queued' || status === 'status_running';
      });

      return idx !== -1;
    }

    getCurrentWorkflow() {
      return _.last(this._workflowsStack);
    }

    clearGraph() {
      this.getCurrentWorkflow().clearGraph();
    }

    updateTypeKnowledge(knowledge) {
      this.getRootWorkflow().updateTypeKnowledge(knowledge);
    }

    updateEdgesStates() {
      this.getCurrentWorkflow().updateEdgesStates(OperationsHierarchyService);
    }

    workflowIsSet() {
      return !_.isNull(this.getRootWorkflow());
    }

    getAllWorkflows() {
      return this._workflowsData;
    }

    getRootWorkflow() {
      return this._workflowsStack[0];
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

  }

  return new WorkflowServiceClass();
}

exports.function = WorkflowService;

exports.inject = function(module) {
  module.factory('WorkflowService', WorkflowService);
};
