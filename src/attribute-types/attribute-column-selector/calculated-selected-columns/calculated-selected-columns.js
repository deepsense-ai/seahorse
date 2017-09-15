'use strict';

function CalculatedSelectedColumns() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/calculated-selected-columns/calculated-selected-columns.html',
    replace: true,
    scope: true,
    controller: function ($element, $scope) {

      function isFieldSelected(field, index, collection) {
        let selectedInGUI = _.any($scope.parameter.items, item => {
          return item.containsField(field, index, collection.length);
        });
        return $scope.parameter.excluding ? !selectedInGUI : selectedInGUI;
      }

      // Add placeholders to given selection. Placeholders are signalling
      // existence of unselected columns between selected ones, for example:
      // Columns in the DataFrame: "a", "b", "c", "d", "e", "f"
      // Selected columns: "b", "d", "f"
      // Result: (...) (b) (...) (d) (...) (f)
      function withPlaceholders(selectedIndices, fieldsLength) {
        let placeholderIndices = [];
        let previousIndex = -1;
        for (let i = 0; i < selectedIndices.length; i++) {
          if (selectedIndices[i] != previousIndex + 1) {
            placeholderIndices.push(selectedIndices[i] - 1);
          }
          previousIndex = selectedIndices[i];
        }
        placeholderIndices.push(fieldsLength - 1);
        selectedIndices = selectedIndices.concat(placeholderIndices);
        selectedIndices.sort((a, b) => a - b);
        return selectedIndices;
      }

      $scope.isSelected = function(field) {
        let collection = $scope.parameter.dataFrameSchema.fields;
        let index = collection.indexOf(field);
        return isFieldSelected(field, index, collection);
      };

      $scope.isPlaceholder = function(field) {
        return !$scope.isSelected(field);
      };

      $scope.allColumns = function() {
        return $scope.parameter.dataFrameSchema.fields;
      };

      $scope.calculatedColumns = function() {
        let fields = $scope.parameter.dataFrameSchema.fields;
        let selectedIndices = [];
        _.forEach(fields, (field, index) => {
          if (isFieldSelected(field, index, fields)) {
            selectedIndices.push(index);
          }
        });
        if (selectedIndices.length > 0) {
          selectedIndices = withPlaceholders(selectedIndices, fields.length);
        }
        return _.filter(fields, (field, index) => _.contains(selectedIndices, index));
      };
    }
  };
}

angular.module('deepsense.attributes-panel').directive('calculatedSelectedColumns', CalculatedSelectedColumns);
