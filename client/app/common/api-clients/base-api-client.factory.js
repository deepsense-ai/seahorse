/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */

'use strict';

/* @ngInject */
function BaseApiClientFactory($http, $q, config) {

  function BaseApiClient() {
  }

  BaseApiClient.prototype.API_URL = config.apiPort ?
    `${config.apiHost}:${config.apiPort}/${config.urlApiVersion}` :
    `${config.apiHost}/${config.urlApiVersion}`;

  BaseApiClient.prototype.METHOD_GET = 'GET';
  BaseApiClient.prototype.METHOD_POST = 'POST';
  BaseApiClient.prototype.METHOD_PUT = 'PUT';
  BaseApiClient.prototype.METHOD_DELETE = 'DELETE';

  /**
   * Performs request to provided url.
   *
   * @param {string} method
   * @param {string} url
   * @param {object} data
   *
   * @return {Promise}
   */
  BaseApiClient.prototype.makeRequest = function (method, url, data = {}) {
    let deferred = $q.defer();
    $http({
      'method': method,
      'url': url,
      'data': data
    }).
      then((result) => {
        deferred.resolve(result.data);
      }, (error) => {
        deferred.reject(error);
      });

    return deferred.promise;
  };

  return BaseApiClient;
}

exports.inject = function (module) {
  module.factory('BaseApiClient', BaseApiClientFactory);
};
