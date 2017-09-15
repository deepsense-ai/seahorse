'use strict';

import tpl from './type-list-selector-item.html';

/* @ngInject */
function TypeListSelectorItem() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    controller: function ($scope) {
      $scope.removeTypesList = function (item) {
        $scope.removeItem($scope.getCurrentItemIndex(item));
      };
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('typeListSelectorItem', TypeListSelectorItem);
