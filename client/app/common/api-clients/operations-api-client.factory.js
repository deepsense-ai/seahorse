'use strict';

/* @ngInject */
function OperationsApiClientFactory(BaseApiClient) {
  const PATH_OPERATIONS = '/operations';
  const PATH_CATALOG = '/operations/catalog';
  const PATH_HIERARCHY = '/operations/hierarchy';

  function OperationsApiClient() {
    BaseApiClient.call(this);
  }

  OperationsApiClient.prototype = Object.create(BaseApiClient.prototype);
  OperationsApiClient.prototype.constructor = OperationsApiClient;

  OperationsApiClient.prototype.getAll = function getAll() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_OPERATIONS);
  };

  OperationsApiClient.prototype.get = function get(id) {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_OPERATIONS + '/' + id);
  };

  OperationsApiClient.prototype.getCatalog = function getCatalog() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_CATALOG);
  };

  OperationsApiClient.prototype.getHierarchy = function getHierarchy() {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_HIERARCHY);
  };

  return new OperationsApiClient();
}


exports.inject = function (module) {
  module.factory('OperationsApiClient', OperationsApiClientFactory);
};
