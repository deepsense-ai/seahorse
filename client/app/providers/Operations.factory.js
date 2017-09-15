/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function OperationsFactory(OperationsAPIClient, $q) {
  var service = {};

  var operationsData = null,
      catalogData = null;


  /**
   * Gets operation data.
   *
   * @param {string} id
   *
   * @return {Object}
   */
  var getOperation = function getOperation(id) {
    return operationsData[id] || null;
  };


  /**
   * Loads operation data from API.
   *
   * @return {Promise}
   */
  var loadData = function loadData() {
    return OperationsAPIClient.getAll().then((data) => {
      operationsData = data.operations;
      return operationsData;
    });
  };


  /**
   * Returns operation data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  service.get = function get(id) {
    if (operationsData) {
      let deferred = $q.defer();
      deferred.resolve(getOperation(id));
      return deferred.promise;
    }

    return loadData().then(() => {
      return getOperation(id);
    });
  };

  /**
   * Returns operations full list.
   *
   * @return {Promise}
   */
  service.getAll = function getAll() {
    if (operationsData) {
      let deferred = $q.defer();
      deferred.resolve(operationsData);
      return deferred.promise;
    }

    return loadData();
  };

  /**
   * Returns operation catalog.
   *
   * @return {Promise}
   */
  service.getCatalog = function getCatalog() {
    if (catalogData) {
      let deferred = $q.defer();
      deferred.resolve(catalogData);
      return deferred.promise;
    }

    return OperationsAPIClient.getCatalog().then((data) => {
      catalogData = data;
      return catalogData;
    });
  };


  return service;
}

exports.inject = function (module) {
  module.factory('Operations', OperationsFactory);
};
