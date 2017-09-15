'use strict';

/* @ngInject */
function WorkflowsApiClientFactory(BaseApiClient, config) {
  const API_TYPE = 'batch';
  const PATH_WORKFLOWS = '/workflows';

  class WorkflowsApiClient extends BaseApiClient {
    constructor() {
      super();
    }

    getWorkflow(id) {
      return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_WORKFLOWS + '/' + id);
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
      return this.makeRequest(this.METHOD_PUT, this.API_URL + PATH_WORKFLOWS + '/' + serializedWorkflow.id, data);
    }

    getDownloadWorkflowUrl(id) {
      return this.API_URL + PATH_WORKFLOWS + '/' + id + '/download?format=json';
    }
  }

  return new WorkflowsApiClient();
}

exports.inject = function (module) {
  module.factory('WorkflowsApiClient', WorkflowsApiClientFactory);
};
