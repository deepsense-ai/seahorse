/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function OperationsAPIClientFactory(BaseAPIClient) {
  const PATH_OPERATIONS = '/operations/';

  function OperationsAPIClient() {
    BaseAPIClient.call(this);
  }
  OperationsAPIClient.prototype = Object.create(BaseAPIClient.prototype);
  OperationsAPIClient.prototype.constructor = OperationsAPIClient;

  /**
   * Returns list of all operations.
   *
   * @return {Promise}
   */
  OperationsAPIClient.prototype.getAll = function() {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_OPERATIONS);
  };

  return new OperationsAPIClient();
}


exports.inject = function (module) {
  module.factory('OperationsAPIClient', OperationsAPIClientFactory);
};
