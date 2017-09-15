'use strict';

/* @ngInject */
function WorkflowsApiClientFactory(BaseApiClient, config) {

  const API_TYPE = 'batch';
  const PATH_EXPERIMENTS = '/experiments';
  const PATH_WORKFLOWS = '/workflows';

  class WorkflowsApiClient extends BaseApiClient {
    constructor() {
      super();
    }

    getData(id) {
      return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_EXPERIMENTS + '/' + id);
    }

    saveData(data) {
      return this.makeRequest(
        this.METHOD_PUT,
        this.API_URL + PATH_EXPERIMENTS + '/' + data.experiment.id,
        data
      );
    }

    createExperiment(params) {
      let data = {
        'experiment': {
          'name': params.name,
          'description': params.description,
          'graph': {
            'nodes': [],
            'edges': []
          }
        }
      };

      return this.makeRequest(
        this.METHOD_POST,
        this.API_URL + PATH_EXPERIMENTS,
        data
      );
    }

    modifyExperiment(id, params) {
      let data = {
        'experiment': {
          'name': params.name,
          'description': params.description,
          'graph': params.graph
        }
      };

      return this.makeRequest(
        this.METHOD_PUT,
        this.API_URL + PATH_EXPERIMENTS + '/' + id,
        data
      );
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
        workflow: {},
        thirdPartyData: {
          gui:{
            name: params.name,
            description: params.description
          }
        }
      };
      return this.makeRequest(this.METHOD_POST, this.API_URL + PATH_WORKFLOWS, data);
    }

    // TODO
    updateWorkflow() {

    }

    // TODO
    uploadReport() {

    }

    // TODO
    downloadWorkflow() {

    }
  }

  return new WorkflowsApiClient();
}

exports.inject = function (module) {
  module.factory('WorkflowsApiClient', WorkflowsApiClientFactory);
};
