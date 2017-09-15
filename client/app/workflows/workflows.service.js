'use strict';

/* @ngInject */
function WorkflowService(Workflow, OperationsHierarchyService, WorkflowsApiClient, Operations) {

  let internal = {};

  class WorkflowServiceClass {

    constructor() {
      // TODO Internal state to be removed
      internal.mainWorkflow = null;
      internal.workflowById = {};
      this.init();
    }

    init() {
      WorkflowsApiClient.getAllWorkflows().then((data) => {
        internal.workflowsData = data;
      });
    }

    getWorkflowById(workflowId) {
      return internal.workflowById[workflowId];
    }

    initMainWorkflow(workflowData) {
      let workflow = this._createWorkflowFromWorkflowData(workflowData);

      internal.mainWorkflow = workflow;
      internal.workflowById[workflow.id] = workflow;

      // TODO Traverse over workflow and add all inner workflows to map.

      return workflow;
    }

    _createWorkflowFromWorkflowData(workflowData) {
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
      let statuses = _.chain(internal.mainWorkflow.getNodes())
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

    getMainWorkflow() {
      return internal.mainWorkflow;
    }

    getPredefColors() {
      return internal.mainWorkflow.predefColors;
    }

    clearGraph() {
      internal.mainWorkflow.clearGraph();
    }

    clearWorkflow() {
      internal.mainWorkflow = null;
    }

    updateTypeKnowledge(knowledge) {
      internal.mainWorkflow.updateTypeKnowledge(knowledge);
    }

    updateEdgesStates() {
      internal.mainWorkflow.updateEdgesStates(OperationsHierarchyService);
    }

    workflowIsSet() {
      return !_.isNull(internal.mainWorkflow);
    }

    getAllWorkflows() {
      return internal.workflowsData;
    }

    saveWorkflow() {
      return WorkflowsApiClient.updateWorkflow(internal.mainWorkflow.serialize());
    }

    removeWorkflowFromList(workflowId) {
      let foundWorkflow = internal.workflowsData.find((workflow) => workflow.id === workflowId);
      let workflowIndex = internal.workflowsData.indexOf(foundWorkflow);
      if (workflowIndex >= 0) {
        internal.workflowsData.splice(workflowIndex, 1);
      }
    }

    deleteWorkflow(workflowId) {
      WorkflowsApiClient.deleteWorkflow(workflowId).then(() => {
        if (internal.mainWorkflow && internal.mainWorkflow.id === workflowId) {
          internal.mainWorkflow = null;
        }
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
