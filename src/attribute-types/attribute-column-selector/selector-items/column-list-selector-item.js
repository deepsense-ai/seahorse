/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function ColumnListSelectorItem() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/selector-items/column-list-selector-item.html',
    replace: true,
    link: function (scope) {
      _.assign(scope, {
        addColumn() {
          scope.item.addColumn('');
        },
        removeColumn(columnIndex) {
          scope.item.columns.splice(columnIndex, 1);
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('columnListSelectorItem', ColumnListSelectorItem);
