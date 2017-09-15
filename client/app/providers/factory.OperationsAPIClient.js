/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function OperationsAPIClientFactory(BaseAPIClient) {

  function OperationsAPIClient() {
    BaseAPIClient.call(this);
  }
  OperationsAPIClient.prototype = Object.create(BaseAPIClient.prototype);
  OperationsAPIClient.prototype.constructor = OperationsAPIClient;

  return new OperationsAPIClient();
}


exports.inject = function (module) {
  module.factory('OperationsAPIClient', OperationsAPIClientFactory);
};
