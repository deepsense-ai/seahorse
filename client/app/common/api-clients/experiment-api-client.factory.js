/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */

'use strict';

/* @ngInject */
function ExperimentApiClientFactory(BaseApiClient) {
  const PATH_EXPERIMENTS = '/experiments';
  const PATH_ACTION = '/action';

  function ExperimentApiClient() {
    BaseApiClient.call(this);
  }

  ExperimentApiClient.prototype = Object.create(BaseApiClient.prototype);
  ExperimentApiClient.prototype.constructor = ExperimentApiClient;

  /**
   * Returns list of experiments.
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.getList = function() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_EXPERIMENTS);
  };

  /**
   * Returns full experiment data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.getData = function(id) {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_EXPERIMENTS + '/' + id);
  };

  /**
   * Saves experiment data.
   *
   * @param {object} data
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.saveData = function(data) {
    return this.makeRequest(
      this.METHOD_PUT,
      this.API_URL + PATH_EXPERIMENTS + '/' + data.experiment.id,
      data
    );
  };

  /**
   * Runs experiment.
   *
   * @param {string} id
   * @param {object} params
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.runExperiment = function(id, params) {
    let data = {
      'launch': {}
    };
    if (params && params.targetNodes) {
      data.launch.targetNodes = params.targetNodes;
    }

    return this.makeRequest(
      this.METHOD_POST,
      this.API_URL + PATH_EXPERIMENTS + '/' + id + PATH_ACTION,
      data
    );
  };

  /**
   * Aborts experiment run.
   *
   * @param {string} id
   * @param {object} params
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.abortExperiment = function(id, params) {
    let data = {
      'abort': {}
    };
    if (params && params.nodes) {
      data.abort.nodes = params.nodes;
    }

    return this.makeRequest(
      this.METHOD_POST,
      this.API_URL + PATH_EXPERIMENTS + '/' + id + PATH_ACTION,
      data
    );
  };

  /**
   * Creates a new experiment.
   *
   * @param {object} params
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.createExperiment = function(params) {
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
  };

  /**
   * Modifies an existing experiment.
   *
   * @param {string} id
   * @param {object} params
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.modifyExperiment = function(id, params) {
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
  };

  /**
   * Deletes an existing experiment.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  ExperimentApiClient.prototype.deleteExperiment = function(id) {
    return this.makeRequest(
      this.METHOD_DELETE,
      this.API_URL + PATH_EXPERIMENTS + '/' + id
    );
  };

  return new ExperimentApiClient();
}


exports.inject = function (module) {
  module.factory('ExperimentApiClient', ExperimentApiClientFactory);
};
