/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

/* @ngInject */
function ModelApiClientFactory(BaseApiClient) {
  const PATH_MODELS = '/models';
  const PATH_DEPLOY = '/deploy';

  function ModelApiClient() {
    BaseApiClient.call(this);
  }

  ModelApiClient.prototype = Object.create(BaseApiClient.prototype);
  ModelApiClient.prototype.constructor = ModelApiClient;

  /**
   * Returns model's data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  ModelApiClient.prototype.deployModel = function deployModel(id) {
    return this.makeRequest(this.METHOD_POST, this.API_URL + PATH_MODELS + '/' + id + PATH_DEPLOY);
  };

  return new ModelApiClient();
}


exports.inject = function (module) {
  module.factory('ModelApiClient', ModelApiClientFactory);
};
