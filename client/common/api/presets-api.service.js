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
