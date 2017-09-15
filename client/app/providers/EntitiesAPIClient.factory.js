/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function EntitiesAPIClientFactory(BaseAPIClient) {
  const PATH_ENTITIES = '/entities';
  const PATH_REPORT = '/report';

  function EntitiesAPIClient() {
    BaseAPIClient.call(this);
  }
  EntitiesAPIClient.prototype = Object.create(BaseAPIClient.prototype);
  EntitiesAPIClient.prototype.constructor = EntitiesAPIClient;

  /**
   * Returns entity report data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  EntitiesAPIClient.prototype.getReport = function getReport(id) {
    return this.makeRequest(this.METHOD_GET, this.API_PATH + PATH_ENTITIES + '/' + id + PATH_REPORT);
  };

  return new EntitiesAPIClient();
}


exports.inject = function (module) {
  module.factory('EntitiesAPIClient', EntitiesAPIClientFactory);
};
