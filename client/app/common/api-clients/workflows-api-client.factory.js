'use strict';

/* @ngInject */
function WorkflowsApiClientFactory(BaseApiClient, ServerCommunication, config, $q) {

  const API_TYPE = 'batch';
  const PATH_WORKFLOWS = '/workflows';

  class WorkflowsApiClient extends BaseApiClient {

    constructor() {
      super();
      this.ServerCommunication = ServerCommunication;
    }

    getAllWorkflows() {
      return this.makeRequest(this.METHOD_GET, `${this.API_URL}${PATH_WORKFLOWS}`, null, 10000);
    }

    getWorkflow(workflowId) {
      return this.makeRequest(this.METHOD_GET, `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}`);
    }

    createWorkflow(params) {
      let data = {
        metadata: {
          type: API_TYPE,
          apiVersion: config.apiVersion
        },
        workflow: {
          nodes: [],
          connections: []
        },
        thirdPartyData: {
          gui: {
            name: params.name,
            description: params.description
          }
        }
      };
      return this.makeRequest(this.METHOD_POST, this.API_URL + PATH_WORKFLOWS, data);
    }

    cloneWorkflow(workflowToClone) {
      return this.makeRequest(this.METHOD_POST, `${this.API_URL}${PATH_WORKFLOWS}/${workflowToClone.id}/clone`, {
        name: workflowToClone.name,
        description: workflowToClone.description
      });
    }

    deleteWorkflow(workflowId) {
      return this.makeRequest(this.METHOD_DELETE, `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}`);
    }

    updateWorkflow(serializedWorkflow) {
      let data = {
        workflowId: serializedWorkflow.id,
        workflow: _.clone(serializedWorkflow)
      };

      data.workflow.metadata = {
        type: API_TYPE,
        apiVersion: config.apiVersion
      };

      this.ServerCommunication.sendUpdateWorkflowToWorkflowExchange(data);
    }

    getDownloadWorkflowMethodUrl(workflowId) {
      return `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/download?format=json`;
    }

    getUploadWorkflowMethodUrl() {
      return `${this.API_URL}${PATH_WORKFLOWS}/upload`;
    }

    cloneNotebookNode(workflowId, sourceNodeId, destinationNodeId) {
      return this.makeRequest(
        this.METHOD_POST,
        `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/notebook/${sourceNodeId}/copy/${destinationNodeId}`
      );
    }

    getPresetByWorkflowId(workflowId) {
      return this.makeRequest(
        this.METHOD_GET,
        `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/preset`
      );
    }

    bindPresetToWorkflow(presetId, workflowId) {
      return this.makeRequest(
        this.METHOD_POST,
        `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/preset`,
        {
          'id': workflowId,
          'presetId': presetId
        }
      );
    }

  }

  return new WorkflowsApiClient();
}

exports.inject = function (module) {
  module.factory('WorkflowsApiClient', WorkflowsApiClientFactory);
};
