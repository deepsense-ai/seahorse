/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function EntitiesAPIClientFactory(BaseAPIClient) {
  function EntitiesAPIClient() {
    BaseAPIClient.call(this);
  }
  EntitiesAPIClient.prototype = Object.create(BaseAPIClient.prototype);
  EntitiesAPIClient.prototype.constructor = EntitiesAPIClient;

  return new EntitiesAPIClient();
}


exports.inject = function (module) {
  module.factory('EntitiesAPIClient', EntitiesAPIClientFactory);
};
