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

import tpl from './index-list-selector-item.html';

/* @ngInject */
function IndexListSelectorItem($document, $timeout) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: true,
    controller: function ($element, $scope) {
      /**
       * TODO: backend API should be changed. Fact that it is built this way causes that frontend code is really
       * complicated. Ideally column selection should always have three items: types, names and ranges.
       */

      /**
       * In fact it changes last added index. Because when you click plus icon
       * in modal, you have added index object to the items array. So when you
       * see field with from and to you in fact redacting this last added index.
       */
      $scope.addIndex = function addIndex(index) {
        let allIndexes = $scope.getItemsThisType('indexRange');

        /**
         * If last added item has values, then it will add new item in order to
         * manage it.
         */
        if (allIndexes[allIndexes.length - 1].secondNum) {
          $scope.addItem('indexRange');
          allIndexes = $scope.getItemsThisType('indexRange');
        }

        angular.extend(allIndexes[allIndexes.length - 1], index);

        index.firstNum = index.secondNum = '';

        $($element)
          .find('[ng-model="index.firstNum"]')
          .focus();

        $scope.addItem('indexRange');
      };

      $scope.removeIndex = function removeIndex(item) {
        $scope.removeItem($scope.getCurrentItemIndex(item));
      };

      $scope.hasValues = function hasValues(indexParam) {
        return !$scope.isEmptyParameter(indexParam);
      };

      $scope.maxIndex = $scope.parameter.dataFrameSchema && $scope.parameter.dataFrameSchema.fields.length - 1;

      $scope.isRangeValid = $scope.maxIndex ?
        function(range) { return range.secondNum <= $scope.maxIndex; } :
        function(range) { return true; };
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('indexListSelectorItem', IndexListSelectorItem);
