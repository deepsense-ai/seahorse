/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function BaseAPIClientFactory($http, $q) {

  function BaseAPIClient() {
  }

  BaseAPIClient.prototype.API_PATH = '/api';

  BaseAPIClient.prototype.METHOD_GET = 'GET';
  BaseAPIClient.prototype.METHOD_POST = 'POST';
  BaseAPIClient.prototype.METHOD_PUT = 'PUT';
  BaseAPIClient.prototype.METHOD_DELETE = 'DELETE';

  /**
   * Performs request to provided url.
   *
   * @param {string} method
   * @param {string} url
   * @param {object} data
   *
   * @return {Promise}
   */
  BaseAPIClient.prototype.makeRequest = function (method, url, data = {}) {
    let deferred = $q.defer();
    $http({
      'method': method,
      'url': url,
      'data': data
    }).then((result) => {
      deferred.resolve(result.data);
    }, (error) => {
      deferred.reject(error);
    });
    return deferred.promise;
  };

  return BaseAPIClient;
}

exports.inject = function (module) {
  module.factory('BaseAPIClient', BaseAPIClientFactory);
};
