/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function OperationsAPIClientFactory(BaseAPIClient) {
  const PATH_OPERATIONS = '/operations';
  const PATH_CATALOG    = '/operations/catalog';

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
  OperationsAPIClient.prototype.getAll = function getAll() {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_OPERATIONS);
  };

  /**
   * Returns operation data.
   *
   * @return {Promise}
   */
  OperationsAPIClient.prototype.get = function get(id) {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_OPERATIONS + '/' + id);
  };

  /**
   * Returns operations catalog data.
   *
   * @return {Promise}
   */
  OperationsAPIClient.prototype.getCatalog = function getCatalog() {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_CATALOG);
  };

  return new OperationsAPIClient();
}


exports.inject = function (module) {
  module.factory('OperationsAPIClient', OperationsAPIClientFactory);
};
