/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function ExperimentAPIClientFactory(BaseAPIClient) {
  const PATH_EXPERIMENTS = '/experiments';
  const PATH_ACTION = '/action';

  function ExperimentAPIClient() {
    BaseAPIClient.call(this);
  }
  ExperimentAPIClient.prototype = Object.create(BaseAPIClient.prototype);
  ExperimentAPIClient.prototype.constructor = ExperimentAPIClient;

  /**
   * Returns list of experiments.
   *
   * @return {Promise}
   */
  ExperimentAPIClient.prototype.getList = function() {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_EXPERIMENTS);
  };

  /**
   * Returns full experiment data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  ExperimentAPIClient.prototype.getData = function(id) {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_EXPERIMENTS + '/' + id);
  };

  /**
   * Saves experiment data.
   *
   * @param {object} data
   *
   * @return {Promise}
   */
  ExperimentAPIClient.prototype.saveData = function(data) {
    return this.makeRequest(
      this.METHOD_PUT,
      this.API_PATH + PATH_EXPERIMENTS + '/' + data.experiment.id,
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
  ExperimentAPIClient.prototype.runExperiment = function(id, params) {
    let data = {
      'launch': {}
    };
    if (params && params.targetNodes) {
      data.launch.targetNodes = params.targetNodes;
    }

    return this.makeRequest(
      this.METHOD_POST,
      this.API_PATH + PATH_EXPERIMENTS + '/' + id + PATH_ACTION,
      data
    );
  };

  return new ExperimentAPIClient();
}


exports.inject = function (module) {
  module.factory('ExperimentAPIClient', ExperimentAPIClientFactory);
};
