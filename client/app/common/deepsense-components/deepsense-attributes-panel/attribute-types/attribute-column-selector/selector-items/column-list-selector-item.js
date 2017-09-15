'use strict';

import tpl from './column-list-selector-item.html';

/* @ngInject */
function ColumnListSelectorItem($timeout) {

  let calculateInvalidColumnsNames = function(scope) {
    let dfSchema = scope.parameter.dataFrameSchema;
    if (!dfSchema) {
      return [];
    }
    return _.filter(scope.item.columns, (column) =>
      !dfSchema.fields.find(field => field.name === column.name)
    );
  };

  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    link: function (scope, element) {
      scope.invalidColumnsNames = calculateInvalidColumnsNames(scope);
      _.assign(scope, {
        addColumn() {
          scope.item.addColumn(scope.item.name);

          $timeout(() => {
            scope.item.name = '';
            $(element)
              .find('[ng-model="item.name"]')
              .focus();
          }, false);
        },
        addSelectedObject(object) {
          if (!object) {
            return;
          }
          scope.item.addColumn(object.originalObject.name);
        },
        removeColumn(columnIndex) {
          scope.item.columns.splice(columnIndex, 1);

          if (scope.item.columns.length === 0) {
            scope.removeItem(scope.getCurrentItemIndex(scope.item));
          }
        },
        isColumnValid(column) {
          return !scope.invalidColumnsNames.find(elem => elem === column);
        }
      });
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('columnListSelectorItem', ColumnListSelectorItem);
