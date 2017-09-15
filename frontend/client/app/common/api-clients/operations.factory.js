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
function OperationsFactory(OperationsApiClient, OperationsHierarchyService, $q, $log) {
  const SINK_OPERATION_ID = 'e652238f-7415-4da6-95c6-ee33808561b2';
  const SOURCE_OPERATION_ID = 'f94b04d7-ec34-42f7-8100-93fe235c89f8';

  const HIDDEN_OPERATION_IDS_ARRAY = [SINK_OPERATION_ID, SOURCE_OPERATION_ID];

  var service = {};
  var isLoaded = false;

  var operationsData = {};
  var catalogData = {};
  var categoryMap = {};

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
        $log.error('error', error);
      });
  };

  var loadCatalog = function loadCatalog() {
    return OperationsApiClient.getCatalog()
      .then((data) => {
        filterOutCatalog(data);
        catalogData = data.catalog;
        categoryMap = {};
        createCategoryMap(catalogData);
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
      .then(loadCatalog)
      .then(() => {
        isLoaded = true;
      });
  };

  service.getData = function getData(id) {
    if (!isLoaded) {
      $log.error('Operations not loaded!');
      return null;
    }
    return operationsData;
  };

  service.get = function get(id) {
    if (!isLoaded) {
      $log.error('Operations not loaded!');
      return null;
    }
    return operationsData[id] || null;
  };

  service.getWithParams = function getWithParams(id) {
    if (!isLoaded) {
      $log.error('Operations not loaded!');
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
      $log.error('Operations not loaded!');
      return null;
    }
    return catalogData;
  };

  service.getCategory = function getCategory(id) {
    if (!isLoaded) {
      $log.error('Operations not loaded!');
      return null;
    }
    return categoryMap[id] || null;
  };

  service.filterCatalog = function (catalog, filter) {
    if (!isLoaded) {
      $log.error('Operations not loaded!');
      return null;
    }
    return catalog.map((catalog) => {
      return Object.assign({}, catalog, {
        catalog: service.filterCatalog(catalog.catalog, filter),
        items: catalog.items.filter(filter)
      });
    }).filter((catalog) => catalog.items.length || catalog.catalog.length);
  };

  service.getFilterForTypeQualifier = function(inputQualifier) {
    return (item) => {
      return service.get(item.id).ports.input.filter((port) =>
        OperationsHierarchyService.IsDescendantOf(inputQualifier, port.typeQualifier)
      ).length;
    };
  };

  return service;
}

exports.inject = function(module) {
  module.factory('Operations', OperationsFactory);
};
