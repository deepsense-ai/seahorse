/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

/* @ngInject */
function BaseApiClientFactory($http, $q, config) {

  function BaseApiClient() {}

  BaseApiClient.prototype.API_URL = config.apiPort ?
    `${config.apiHost}:${config.apiPort}/${config.urlApiVersion}` :
    `${config.apiHost}/${config.urlApiVersion}`;

  BaseApiClient.prototype.METHOD_GET = 'GET';
  BaseApiClient.prototype.METHOD_POST = 'POST';
  BaseApiClient.prototype.METHOD_PUT = 'PUT';
  BaseApiClient.prototype.METHOD_DELETE = 'DELETE';

  BaseApiClient.prototype.makeRequest = function(method, url, data = {}, timeout) {
    let deferred = $q.defer();
    $http({
        'method': method,
        'url': url,
        'data': data,
        'timeout': timeout
      })
      .then((result) => {
        deferred.resolve(result.data);
      }, (error) => {
        deferred.reject(error);
      });

    return deferred.promise;
  };

  return BaseApiClient;
}

exports.inject = function(module) {
  module.factory('BaseApiClient', BaseApiClientFactory);
};
