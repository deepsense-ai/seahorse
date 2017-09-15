'use strict';

/*@ngInject*/
function ColumnListSelectorItem($timeout) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/selector-items/column-list-selector-item.html',
    replace: true,
    link: function (scope, element) {
      _.assign(scope, {
        addColumn() {
          scope.item.addColumn(scope.item.name);

          $timeout(() => {
            scope.item.name = '';
            $(element).find('[ng-model="item.name"]').focus();
          }, false);
        },
        removeColumn(columnIndex) {
          scope.item.columns.splice(columnIndex, 1);

          if (scope.item.columns.length === 0) {
            scope.removeItem(scope.getCurrentItemIndex(scope.item));
          }
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('columnListSelectorItem', ColumnListSelectorItem);
