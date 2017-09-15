'use strict';

/* @ngInject */
function WorkflowService(Workflow, OperationsHierarchyService,
  WorkflowsApiClient, Operations, UUIDGenerator,
  $rootScope) {

  let internal = {};

  class WorkflowServiceClass {

    constructor() {
      internal.workflow = null;
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
      internal.workflow = workflow;
      return workflow;
    }

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

    saveWorkflow() {
      return WorkflowsApiClient
        .updateWorkflow(internal.workflow.serialize())
        .then((data) => {
          if (this.workflowIsSet()) {
            $rootScope.$broadcast('Workflow.SAVE.SUCCESS', data);
            return data;
          }
        })
        .catch((error) => {
          $rootScope.$broadcast('Workflow.SAVE.ERROR', error);
        });
    }

    cloneParamsFromNodeToNode(nodeSrc, nodeDist) {
      nodeDist.parametersValues = nodeSrc.parameters.serialize();
    }

    cloneNode(node) {
      let operation = Operations.get(node.operationId);
      let offset = {
        x: 255,
        y: 0
      };
      let nodeClone = _.cloneDeep(node);
      let nodeParams = angular.merge(
        nodeClone, {
          'id': UUIDGenerator.generateUUID(),
          'operation': operation,
          'x': node.x - offset.x >= 0 ? node.x - offset.x : node.x,
          'y': node.y - offset.y >= 0 ? node.y - offset.y : node.y,
          'uiName': nodeClone.uiName ? nodeClone.uiName += ' copy' : ''
        }
      );
      let createdNode = this.getWorkflow().createNode(nodeParams);

      /* Copy params */
      this.cloneParamsFromNodeToNode(node, createdNode);

      return this.getWorkflow().addNode(createdNode);
    }
  }

  return new WorkflowServiceClass();
}

exports.function = WorkflowService;

exports.inject = function(module) {
  module.factory('WorkflowService', WorkflowService);
};
