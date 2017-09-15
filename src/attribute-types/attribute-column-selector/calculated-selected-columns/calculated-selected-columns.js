'use strict';

function CalculatedSelectedColumns() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/calculated-selected-columns/calculated-selected-columns.html',
    replace: true,
    scope: true,
    controller: function ($element, $scope) {
      $scope.calculatedColumns = function() {
        let fieldsCount = $scope.parameter.dataFrameSchema.fields.length;
        return _.filter($scope.parameter.dataFrameSchema.fields, (field, index) =>
          _.any($scope.parameter.items, item =>
            item.containsField(field, index, fieldsCount)
          )
        );
      };
    }
  };
}

angular.module('deepsense.attributes-panel').directive('calculatedSelectedColumns', CalculatedSelectedColumns);
