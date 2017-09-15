/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */

'use strict';

/* @ngInject */
function WorkflowsApiClientFactory(BaseApiClient) {
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

    // TODO
    getWorkflow(id) {
      return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_WORKFLOWS + '/' + id);
    }

    // TODO
    createWorkflow() {
      return this.makeRequest(this.METHOD_POST, this.API_URL + PATH_WORKFLOWS);
    }

    // TODO
    updateWorkflow() {

    }

    // TODO
    uploadWorkflow() {

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
