'use strict';

/*@ngInject*/
function TypeListSelectorItem() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/selector-items/type-list-selector-item.html',
    replace: true,
    controller: function ($scope) {
      $scope.removeTypesList = function (item) {
        $scope.removeItem($scope.getCurrentItemIndex(item));
      };
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('typeListSelectorItem', TypeListSelectorItem);
