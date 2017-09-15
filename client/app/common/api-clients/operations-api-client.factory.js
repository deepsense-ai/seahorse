/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */

'use strict';

/* @ngInject */
function OperationsApiClientFactory(BaseApiClient) {
  const PATH_OPERATIONS = '/operations';
  const PATH_CATALOG    = '/operations/catalog';
  const PATH_HIERARCHY  = '/operations/hierarchy';

  function OperationsApiClient() {
    BaseApiClient.call(this);
  }

  OperationsApiClient.prototype = Object.create(BaseApiClient.prototype);
  OperationsApiClient.prototype.constructor = OperationsApiClient;

  /**
   * Returns list of all operations.
   *
   * @return {Promise}
   */
  OperationsApiClient.prototype.getAll = function getAll() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_OPERATIONS);
  };

  /**
   * Returns operation data.
   *
   * @return {Promise}
   */
  OperationsApiClient.prototype.get = function get(id) {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_OPERATIONS + '/' + id);
  };

  /**
   * Returns operations catalog data.
   *
   * @return {Promise}
   */
  OperationsApiClient.prototype.getCatalog = function getCatalog() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_CATALOG);
  };

  /**
   * Returns operation's hierarchy data.
   *
   * @return {Promise}
   */
  OperationsApiClient.prototype.getHierarchy = function getHierarchy() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_HIERARCHY);
  };

  return new OperationsApiClient();
}


exports.inject = function (module) {
  module.factory('OperationsApiClient', OperationsApiClientFactory);
};
