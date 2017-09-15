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
    controller: function ($scope) {
      const indexesContainerSelector = `.o-modal-selector__list--${$scope.itemType.id.toLowerCase()}`;
      const allButtonsExceptLastSelector =
        `${indexesContainerSelector}:not(:last) [data-action="add-range"]`;
      const lastButtonSelector = `${indexesContainerSelector}:last [data-action="add-range"]`;

      if ($scope.getItemsThisType($scope.itemType.id).length > 1) {
        $($document).find(allButtonsExceptLastSelector).hide();
      }

      $scope.removeIndex = function (item) {
        $scope.removeItem($scope.getCurrentItemIndex(item));
        $timeout(() => {
          $($document).find(lastButtonSelector).show();
        }, false);
      };
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('indexListSelectorItem', IndexListSelectorItem);
