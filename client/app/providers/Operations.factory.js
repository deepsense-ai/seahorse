/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


/* @ngInject */
function OperationsFactory(OperationsAPIClient, $q) {
  const CATEGORY_ICONS = {
    '11111-5555-9999': 'fa-cube',
    '23123-12312-43242': 'fa-download',
    '91111-111111-11111': 'fa-filter',
    '234234-756756-34234': 'fa-gear'
  };
  const DEFAULT_ICON = 'fa-square';


  var service = {};

  var operationsData = null,
      catalogData = null,
      categoryMap = null;


  /**
   * Gets operation data.
   *
   * @param {string} id
   *
   * @return {Object}
   */
  var getOperationData = function getOperationData(id) {
    return operationsData[id] || null;
  };

  /**
   * Gets operation category data.
   *
   * @param {string} id
   *
   * @return {Object}
   */
  var getCategoryData = function getCategoryData(id) {
    return categoryMap[id] || null;
  };


  /**
   * Creates category map.
   *
   * @param {object} catalog
   * @param {string} parentId
   */
  var createCategoryMap = function createCategoryMap(catalog, parentId) {
    for (let i = catalog.length - 1; i >= 0; i--) {
      let category = catalog[i];
      categoryMap[category.id] = category;
      if (parentId) {
        category.parentId = parentId;
      }
      if (category.catalog) {
        createCategoryMap(category.catalog, category.id);
      }
    }
  };

  /**
   * Updates each category with its own icon or inherited from parent category.
   */
  var updateCategoryIcons = function updateCategoryIcons() {
    for (let id in categoryMap) {
      let category = categoryMap[id];
      if (CATEGORY_ICONS[id]) {
        category.icon = CATEGORY_ICONS[id];
      } else {
        let parentId = category.parentId;
        while (parentId && categoryMap[parentId]) {
          if (CATEGORY_ICONS[parentId]) {
            category.icon = CATEGORY_ICONS[parentId];
            break;
          }
          parentId = categoryMap[parentId].parentId;
        }
        if (!category.icon) {
          category.icon = DEFAULT_ICON;
        }
      }
    }
  };

  /**
   * Updates each operation with its category icon.
   */
  var updateOperationIcons = function updateOperationIcons() {
    for (let id in operationsData) {
      let operation = operationsData[id],
          category = categoryMap && categoryMap[operation.category];
      operation.icon = category ? category.icon : DEFAULT_ICON;
    }
  };


  /**
   * Loads operation data from API.
   *
   * @return {Promise}
   */
  var loadData = function loadData() {
    return OperationsAPIClient.getAll().then((data) => {
      operationsData = data.operations;
      updateOperationIcons();
      Object.freeze(operationsData);
      return operationsData;
    });
  };

  /**
   * Loads catalog & category data.
   *
   * @return {Promise}
   */
  var loadCatalog = function loadCatalog() {
    return OperationsAPIClient.getCatalog().then((data) => {
      catalogData = data.catalog;
      categoryMap = {};
      createCategoryMap(catalogData);
      updateCategoryIcons();
      updateOperationIcons();
      Object.freeze(catalogData);
      Object.freeze(categoryMap);
      return catalogData;
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
      deferred.resolve(getOperationData(id));
      return deferred.promise;
    }

    return loadData().then(() => {
      return getOperationData(id);
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

    return loadCatalog();
  };

  /**
   * Returns category data.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  service.getCategory = function getCategory(id) {
    if (catalogData) {
      let deferred = $q.defer();
      deferred.resolve(getCategoryData(id));
      return deferred.promise;
    }

    return loadCatalog().then(() => {
      return getCategoryData(id);
    });
  };


  return service;
}

exports.inject = function (module) {
  module.factory('Operations', OperationsFactory);
};
