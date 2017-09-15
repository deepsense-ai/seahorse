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

// App
import ApiBaseClass from './api-base.class';


// Seahorse Datasource Manager API
export default class DatasourcesApiService extends ApiBaseClass {
  constructor($http, config) {
    'ngInject';

    super($http, config);
    this.servicePath = '/datasourcemanager/v1';
  }


  /**
   * Returns list of all datasources
   * @return {Promise}
   */
  getDatasources() {
    const endpointUrl = this.makeEndpointUrl('/datasources');

    return this.$http
      .get(endpointUrl)
      .then(this.getData);
  }


  /**
   * Returns datasources for given id
   * @param  {string} datasourceId  uuid v4
   * @return {Promise}
   */
  getDatasource(datasourceId) {
    const endpointUrl = this.makeEndpointUrl(`/datasources/${datasourceId}`);

    return this.$http
      .get(endpointUrl)
      .then(this.getData);
  }


  /**
   * Creates a new datasource or overrides datasource for given id
   * @param  {string} datasourceId      uuid v4
   * @param  {Object} datasourceParams
   * @return {Promise}
   */
  putDatasource(datasourceId, datasourceParams) {
    const endpointUrl = this.makeEndpointUrl(`/datasources/${datasourceId}`);

    return this.$http
      .put(endpointUrl, datasourceParams)
      .then(this.getData);
  }


  /**
   * Delete datasource for given id
   * @param  {string} datasourceId  uuid v4
   * @return {Promise}
   */
  deleteDatasource(datasourceId) {
    const endpointUrl = this.makeEndpointUrl(`/datasources/${datasourceId}`);

    return this.$http
      .delete(endpointUrl);
  }
}
