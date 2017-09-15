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

exports.inject = function(module) {
  module.factory('OperationsApiClient', OperationsApiClientFactory);
};
