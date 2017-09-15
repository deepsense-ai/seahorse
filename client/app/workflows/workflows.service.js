'use strict';

/* @ngInject */
function WorkflowService(Workflow, OperationsHierarchyService, WorkflowsApiClient, $rootScope) {
  let internal = {};

  class WorkflowServiceClass {
    constructor() {
      internal.workflow = null;
    }

    createWorkflow(workflowData, operations) {
      let workflow = new Workflow();

      workflow.setData({
        'id': workflowData.id,
        'name': workflowData.thirdPartyData.gui.name,
        'description': workflowData.thirdPartyData.gui.description
      });
      workflow.createNodes(workflowData.workflow.nodes, operations, workflowData.thirdPartyData);
      workflow.createEdges(workflowData.workflow.connections);
      // TODO
      //workflow.updateState(workflowData.workflow.state);
      workflow.updateEdgesStates(OperationsHierarchyService);

      internal.workflow = workflow;
    }

    getWorkflow () {
      return internal.workflow;
    }

    clearGraph() {
      internal.workflow.clearGraph();
    }

    clearWorkflow() {
      internal.workflow = null;
    }

    updateTypeKnowledge (knowledge) {
      internal.workflow.updateTypeKnowledge(knowledge);
    }

    updateEdgesStates() {
      internal.workflow.updateEdgesStates(OperationsHierarchyService);
    }

    workflowIsSet () {
      return !_.isNull(internal.workflow);
    }

    saveWorkflow() {
      return WorkflowsApiClient.
        updateWorkflow(internal.workflow.serialize()).
        then((data) => {
          if (this.workflowIsSet()) {
            $rootScope.$broadcast('Workflow.SAVE.SUCCESS');
            return data;
          }
        }).
        catch((error) => {
          $rootScope.$broadcast('Workflow.SAVE.ERROR', error);
        });
    }
  }

  return new WorkflowServiceClass();
}

exports.function = WorkflowService;

exports.inject = function (module) {
  module.factory('WorkflowService', WorkflowService);
};
