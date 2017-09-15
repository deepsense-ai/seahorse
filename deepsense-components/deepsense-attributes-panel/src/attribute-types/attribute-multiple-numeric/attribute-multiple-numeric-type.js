'use strict';

/*@ngInject*/
function AttributeMultipleNumericType($timeout) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-multiple-numeric/attribute-multiple-numeric-type.html',
    replace: true,
    link: function (scope) {
      if (!scope.parameter.value) {
        scope.rawValue = scope.parameter.convertToRawValue(scope.parameter.defaultValue);
      } else {
        scope.rawValue = scope.parameter.convertToRawValue(scope.parameter.value);
      }

      scope.$watch('rawValue', function (newValue, oldValue) {
        let value = scope.parameter.parseRawValue(newValue);
        if (scope.parameter.validateValue(value)) {
          scope.parameter.value = value;
        } else {
          scope.parameter.value = null;
        }
      });

      scope.$watch('parameter.value', function (newValue) {
        scope.isDefault = _.isEqual(scope.parameter.value, scope.parameter.defaultValue);
      });
    }
  };
}

angular.module('deepsense.attributes-panel').directive('attributeMultipleNumericType', AttributeMultipleNumericType);
