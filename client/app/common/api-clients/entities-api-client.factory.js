/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */

'use strict';

/* @ngInject */
function EntitiesApiClientFactory(BaseApiClient) {
  const PATH_ENTITIES = '/entities';
  const PATH_REPORT = '/report';

  function EntitiesApiClient() {
    BaseApiClient.call(this);
  }

  EntitiesApiClient.prototype = Object.create(BaseApiClient.prototype);
  EntitiesApiClient.prototype.constructor = EntitiesApiClient;

  /**
   * Returns entity report data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  EntitiesApiClient.prototype.getReport = function getReport(id) {
    return this.makeRequest(this.METHOD_GET, this.API_URL + PATH_ENTITIES + '/' + id + PATH_REPORT);
  };

  return new EntitiesApiClient();
}


exports.inject = function (module) {
  module.factory('EntitiesApiClient', EntitiesApiClientFactory);
};
