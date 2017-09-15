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


  var service = {},
      isLoaded = false;

  var operationsData = {},
      catalogData = {},
      categoryMap = {};


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
          category = categoryMap[operation.category];
      operation.icon = category ? category.icon : DEFAULT_ICON;
    }
  };


  /**
   * Loads operations list from API.
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
   * Loads full operation data from API.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  var loadOperationData = function loadOperationData(id) {
    return OperationsAPIClient.get(id).then((data) => {
      operationsData[id].parameters = Object.freeze(data.operation.parameters || {});
      Object.freeze(operationsData[id]);
      return operationsData[id];
    }, (error) => {
      console.error('error', error);
    });
  };

  /**
   * Loads catalog & category data from API.
   *
   * @return {Promise}
   */
  var loadCatalog = function loadCatalog() {
    return OperationsAPIClient.getCatalog().then((data) => {
      catalogData = data.catalog;
      categoryMap = {};
      createCategoryMap(catalogData);
      updateCategoryIcons();
      Object.freeze(catalogData);
      Object.freeze(categoryMap);
      return catalogData;
    });
  };


  /**
   * Loads operations catalog & data.
   *
   * @return {Promise}
   */
  service.load = function load() {
    if (isLoaded) {
      let deferred = $q.defer();
      deferred.resolve();
      return deferred.promise;
    }
    return $q.all([
      loadCatalog(),
      loadData()
    ]).then(() => {
      isLoaded = true;
    });
  };

  /**
   * Returns operations list data.
   *
   * @return {object}
   */
  service.getData = function getData(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return operationsData;
  };

  /**
   * Returns operation data.
   *
   * @param {string} id
   *
   * @return {object}
   */
  service.get = function get(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return operationsData[id] || null;
  };

  /**
   * Returns operation data with params schema.
   *
   * @param {string} id
   *
   * @return {Promise}
   */
  service.getWithParams = function getWithParams(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
    }
    let operation = operationsData[id] || null;
    if (!isLoaded || (operation && operation.parameters)) {
      let deferred = $q.defer();
      deferred.resolve(operation);
      return deferred.promise;
    }
    return loadOperationData(id);
  };

  /**
   * Returns category catalog data.
   *
   * @param {string} id
   *
   * @return {object}
   */
  service.getCatalog = function getCatalog(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return catalogData;
  };

  /**
   * Returns category data.
   *
   * @param {string} id
   *
   * @return {object}
   */
  service.getCategory = function getCategory(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return categoryMap[id] || null;
  };


  return service;
}

exports.inject = function (module) {
  module.factory('Operations', OperationsFactory);
};
