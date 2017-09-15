/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function IndexListSelectorItem($document, $timeout) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/selector-items/index-list-selector-item.html',
    replace: true,
    scope: true,
    controller: function ($element, $scope) {
      /**
       * In fact it changes last added index. Because when you click plus icon
       * in modal, you have added index object to the items array. So when you
       * see field with from and to you in fact redacting this last added index.
       */
      $scope.addIndex = function addIndex (index) {
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

        $($element).find('[ng-model="index.firstNum"]').focus();

        $scope.addItem('indexRange');
      };

      $scope.removeIndex = function removeIndex (item) {
        $scope.removeItem($scope.getCurrentItemIndex(item));
      };

      $scope.hasValues = function hasValues (indexParam) {
        return !$scope.isEmptyParameter(indexParam);
      };
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('indexListSelectorItem', IndexListSelectorItem);
