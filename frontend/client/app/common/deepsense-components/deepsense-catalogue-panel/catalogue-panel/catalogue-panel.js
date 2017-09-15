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

import tpl from './catalogue-panel.html';

function OperationCatalogue() {
  return {
    scope: {
      catalogCollection: '=',
      // TODO RENAME
      // OperationCatalogue should not even know what 'running' concept mean.
      // On this abstraction level it should be called 'disabledMode' (like in attribute panel) or something.
      isRunning: '='
    },
    templateUrl: tpl,
    replace: 'true',
    controllerAs: 'cpCtrl',
    controller: ['$scope', '$filter', function($scope, $filter) {

      var cpCtrl = this;
      cpCtrl.search = '';

      cpCtrl.clearSearch = function() {
        cpCtrl.search = '';
      };

      cpCtrl._filterOut = function(tree, filterQuery) {
        let newTree = angular.copy(tree);
        newTree.catalog = _
          .chain(newTree.catalog)
          .map(c => cpCtrl._filterOut(c, filterQuery))
          .filter(c => !_.isNull(c))
          .value();
        newTree.items = _.filter(newTree.items, (item) => {
          return item.name.toLowerCase().indexOf(filterQuery.toLowerCase()) > -1;
        });
        if (newTree.catalog.length === 0 && newTree.items.length === 0) {
          return null;
        } else {
          return newTree;
        }
      };

      $scope.$watch('cpCtrl.search', function (val) {
        let catalogs = $scope.catalogCollection;
        let tree = {catalog: catalogs, items: []};
        let data = cpCtrl._filterOut(tree, val);
        $scope.filteredData = data ? data.catalog : [];
      });
    }]
  };
}
angular.module('deepsense-catalogue-panel').directive('operationCatalogue', OperationCatalogue);

