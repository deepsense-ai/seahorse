/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function ColumnListSelectorItem() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-column-selector/selector-items/column-list-selector-item.html',
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

exports.inject = function (module) {
  module.directive('columnListSelectorItem', ColumnListSelectorItem);
};
