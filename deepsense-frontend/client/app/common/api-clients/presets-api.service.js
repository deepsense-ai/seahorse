'use strict';

/* @ngInject */
function PresetsApi($http, config) {
  const URL = `${config.apiHost}:${config.apiPort}/${config.urlApiVersion}/presets`;
  const service = this;

  service.getAll = getAll;
  service.get = get;
  service.create = create;
  service.update = update;
  service.remove = remove;

  /**
   * @returns {Promise}
   */
  function getAll() {
    return $http.get(URL).then(function processResult(result){
      return result.data;
    });
  }

  /**
   * @param {String} id
   * @returns {Promise}
   */
  function get(id) {
    return $http.get(`${URL}/${id}`).then(function processResult(result){
      return result.data;
    });
  }

  /**
   * @param {Object} preset
   * @returns {Promise}
   */
  function create(preset) {
    return $http.post(`${URL}`, preset);

  }

  /**
   * @param {Number} id
   * @param {Object} preset
   * @returns {Promise}
   */
  function update(id, preset) {
    return $http.post(`${URL}/${id}`, preset);
  }

  /**
   * @param {Number} id
   * @returns {Promise}
   */
  function remove(id) {
    return $http.delete(`${URL}/${id}`);
  }
}

exports.inject = function(module) {
  module.service('PresetsApiService', PresetsApi);
};
