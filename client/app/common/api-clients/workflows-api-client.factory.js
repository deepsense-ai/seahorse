'use strict';

/* @ngInject */
function WorkflowsApiClientFactory(BaseApiClient, config) {
  const API_TYPE = 'batch';
  const PATH_WORKFLOWS = '/workflows';
  const PATH_REPORTS = '/reports';

  class WorkflowsApiClient extends BaseApiClient {
    constructor() {
      super();
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

    updateWorkflow(serializedWorkflow) {
      let data = {
        metadata: {
          type: API_TYPE,
          apiVersion: config.apiVersion
        },
        workflow: serializedWorkflow.workflow,
        thirdPartyData: serializedWorkflow.thirdPartyData
      };
      return this.makeRequest(this.METHOD_PUT, `${this.API_URL}${PATH_WORKFLOWS}/${serializedWorkflow.id}`, data);
    }

    getDownloadWorkflowUrl(workflowId) {
      return `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/download?format=json`;
    }

    getLatestReport(workflowId) {
      return this.makeRequest(this.METHOD_GET, `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/report`);
    }

    getReport(reportId) {
      return this.makeRequest(this.METHOD_GET, `${this.API_URL}${PATH_REPORTS}/${reportId}`);
    }
  }

  return new WorkflowsApiClient();
}

exports.inject = function (module) {
  module.factory('WorkflowsApiClient', WorkflowsApiClientFactory);
};
