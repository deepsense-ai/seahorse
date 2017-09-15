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
          scope.item.addColumn('');

          $timeout(() => {
            $(element).find('.column-list-values input:last').focus();
          }, false);
        },
        removeColumn(columnIndex) {
          scope.item.columns.splice(columnIndex, 1);

          if (scope.item.columns.length === 0) {
            scope.removeItem(scope.getCurrentItemIndex(scope.item));
          }
        }
      });

      if (scope.item.columns && scope.item.columns.length === 0) {
        scope.item.addColumn('');
      }
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('columnListSelectorItem', ColumnListSelectorItem);
