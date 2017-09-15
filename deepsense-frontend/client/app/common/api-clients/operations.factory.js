'use strict';

/* @ngInject */
function OperationsFactory(OperationsApiClient, $q) {
  const CATEGORY_ICONS = {
    '5a39e324-15f4-464c-83a5-2d7fba2858aa': 'fa-exchange', // Input/Output
    '3fcc6ce8-11df-433f-8db3-fa1dcc545ed8': 'fa-bolt', // Transformation
    '6c730c11-9708-4a84-9dbd-3845903f32ac': 'fa-pencil-square-o', // Data Manipulation
    'c80397a8-7840-4bdb-83b3-dc12f1f5bc3c': 'fa-line-chart', // Regression
    'ff13cbbd-f4ec-4df3-b0c3-f6fd4b019edf': 'fa-tag', // Classification
    'dd29042a-a32c-4948-974f-073c41230da0': 'fa-filter' // Feature Selection
  };
  const DEFAULT_ICON = 'fa-square';

  const SINK_OPERATION_ID = 'e652238f-7415-4da6-95c6-ee33808561b2';
  const SOURCE_OPERATION_ID = 'f94b04d7-ec34-42f7-8100-93fe235c89f8';

  const HIDDEN_OPERATION_IDS_ARRAY = [SINK_OPERATION_ID, SOURCE_OPERATION_ID];

  var service = {},
    isLoaded = false;

  var operationsData = {},
    catalogData = {},
    categoryMap = {};

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

  var updateOperationIcons = function updateOperationIcons() {
    for (let id in operationsData) {
      let operation = operationsData[id];
      let category = categoryMap[operation.category];
      operation.icon = category ? category.icon : DEFAULT_ICON;
    }
  };

  var loadData = function loadData() {
    return OperationsApiClient.getAll()
      .then((data) => {
        let sinkOperation = data.operations[SINK_OPERATION_ID];
        if (sinkOperation) {
          removeOutputPortsForSinkOperation(sinkOperation);
        }
        operationsData = data.operations;
        Object.freeze(operationsData);
        return operationsData;
      });
  };

  var loadOperationData = function loadOperationData(id) {
    return OperationsApiClient.get(id)
      .then((data) => {
        if (_.isUndefined(operationsData[id].parameters)) {
          removeOutputPortsForSinkOperation(data.operation);
          operationsData[id].parameters = Object.freeze(data.operation.parameters || {});
          Object.freeze(operationsData[id]);
        }
        return operationsData[id];
      }, (error) => {
        console.error('error', error);
      });
  };

  var loadCatalog = function loadCatalog() {
    return OperationsApiClient.getCatalog()
      .then((data) => {
        filterOutCatalog(data);
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

  // FIXME Backend reuses catalog do look-up in operation/{id} methods.
  // Source and Sink operations should be accessible through id, but should
  // not be part of catalog. As a workaround it's getting filtered out here.
  var filterOutCatalog = function(catalog) {
    // Catalog have tree structure, where every node have array of catalogs (named 'catalog') and items.
    _.forEach(catalog.catalog, (catalog) => filterOutCatalog(catalog));

    let filteredItems = _.filter(catalog.items, (item) => HIDDEN_OPERATION_IDS_ARRAY.indexOf(item.id) === -1);
    catalog.items = filteredItems;
  };

  // Due to backend design flaw operation API says that SINK operation has one output port.
  // Eventually we probably will fix that. For now we are hacking it around in frontend
  // by manually removing output ports for sink operation.
  // TODO Remove it once API is fixed
  function removeOutputPortsForSinkOperation(operation) {
    if (operation.id === SINK_OPERATION_ID) {
      operation.ports.output = [];
    }
  }

  service.load = function load() {
    if (isLoaded) {
      let deferred = $q.defer();
      deferred.resolve();
      return deferred.promise;
    }

    return loadData()
      .
    then(loadCatalog)
      .
    then(() => {
      isLoaded = true;
    });
  };

  service.getData = function getData(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return operationsData;
  };

  service.get = function get(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return operationsData[id] || null;
  };

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

  service.hasWithParams = function hasWithParams(id) {
    let operation = operationsData[id] || null;
    return !!(isLoaded && operation && operation.parameters);
  };

  service.getCatalog = function getCatalog(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return catalogData;
  };

  service.getCategory = function getCategory(id) {
    if (!isLoaded) {
      console.error('Operations not loaded!');
      return null;
    }
    return categoryMap[id] || null;
  };

  return service;
}

exports.inject = function(module) {
  module.factory('Operations', OperationsFactory);
};
