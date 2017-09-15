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


export default class PresetsApiService extends ApiBaseClass {
  constructor($http, config) {
    'ngInject';

    super($http, config);
    this.servicePath = `/${config.urlApiVersion}/presets`;
  }


  /**
   * @returns {Promise}
   */
  getAll() {
    const endpointUrl = this.makeEndpointUrl();

    return this.$http
      .get(endpointUrl)
      .then((result) => result.data);
  }


  /**
   * @param {String} presetId
   * @returns {Promise}
   */
  get(presetId) {
    const endpointUrl = this.makeEndpointUrl(`/${presetId}`);

    return this.$http
      .get(endpointUrl)
      .then((result) => result.data);
  }


  /**
   * @param {Object} preset
   * @returns {Promise}
   */
  create(preset) {
    const endpointUrl = this.makeEndpointUrl();

    return this.$http
      .post(endpointUrl, preset);
  }


  /**
   * @param {Number} presetId
   * @param {Object} preset
   * @returns {Promise}
   */
  update(presetId, preset) {
    const endpointUrl = this.makeEndpointUrl(`/${presetId}`);

    return this.$http
      .post(endpointUrl, preset);
  }


  /**
   * @param {Number} presetId
   * @returns {Promise}
   */
  remove(presetId) {
    const endpointUrl = this.makeEndpointUrl(`/${presetId}`);

    return this.$http
      .delete(endpointUrl);
  }
}
