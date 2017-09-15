'use strict';

/* @ngInject */
function WorkflowService(Workflow, OperationsHierarchyService, WorkflowsApiClient) {

  let internal = {};

  class WorkflowServiceClass {

    constructor() {
      // TODO Internal state to be removed
      internal.workflow = null;
      this.init();
    }

    init() {
      WorkflowsApiClient.getAllWorkflows().then((data) => {
        internal.workflows = data;
      });
    }

    createWorkflow(workflowData, operations) {
      let workflow = new Workflow();
      let thirdPartyData = workflowData.thirdPartyData || {};
      workflow.id = workflowData.id;
      workflow.name = (thirdPartyData.gui || {}).name;
      workflow.description = (thirdPartyData.gui || {}).description;
      workflow.predefColors = (thirdPartyData.gui || {}).predefColors || workflow.predefColors;
      workflow.createNodes(workflowData.workflow.nodes, operations, workflowData.thirdPartyData);
      workflow.createEdges(workflowData.workflow.connections);
      workflow.updateEdgesStates(OperationsHierarchyService);
      // TODO Internal state to be removed
      internal.workflow = workflow;
      return workflow;
    }

    isWorkflowRunning() {
      let statuses = _.chain(internal.workflow.getNodes())
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

    // TODO Internal state to be removed
    getWorkflow() {
      return internal.workflow;
    }

    getPredefColors() {
      return internal.workflow.predefColors;
    }

    clearGraph() {
      internal.workflow.clearGraph();
    }

    clearWorkflow() {
      internal.workflow = null;
    }

    updateTypeKnowledge(knowledge) {
      internal.workflow.updateTypeKnowledge(knowledge);
    }

    updateEdgesStates() {
      internal.workflow.updateEdgesStates(OperationsHierarchyService);
    }

    workflowIsSet() {
      return !_.isNull(internal.workflow);
    }

    getAllWorkflows() {
      return internal.workflows;
    }

    saveWorkflow() {
      return WorkflowsApiClient.updateWorkflow(internal.workflow.serialize());
    }

    removeWorkflowFromList(workflowId) {
      let foundWorkflow = internal.workflows.find((workflow) => workflow.id === workflowId);
      let workflowIndex = internal.workflows.indexOf(foundWorkflow);
      if (workflowIndex >= 0) {
        internal.workflows.splice(workflowIndex, 1);
      }
    }

    deleteWorkflow(workflowId) {
      WorkflowsApiClient.deleteWorkflow(workflowId).then(() => {
        if (internal.workflow && internal.workflow.id === workflowId) {
          internal.workflow = null;
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
