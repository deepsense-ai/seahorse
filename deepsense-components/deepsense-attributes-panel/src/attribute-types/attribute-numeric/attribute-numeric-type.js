'use strict';

/*@ngInject*/
function AttributeNumericType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-numeric/attribute-numeric-type.html',
    replace: true,
    link: function (scope, element) {

      if (_.isUndefined(scope.parameter.value) || _.isNull(scope.parameter.value)) {
        scope.parameter.value = scope.parameter.defaultValue;
      }
      
      // These watches are set to avoid setting undefined parameter value
      // while typing minus, dot or plus signs. Null value is set instead.
      scope.$watch('parameter.value', function (newValue) {
        scope.valueBuffer = newValue;
      });

      scope.$watch('valueBuffer', function (newValue) {
        if (_.isUndefined(newValue)) {
          scope.parameter.value = null;
        } else {
          scope.parameter.value = newValue;
        }
      });

      let validator = scope.parameter.validator;
      if (validator && validator.schema && validator.schema.type === 'range') {
        element[0].children[0].setAttribute('step', validator.schema.configuration.step || 0.1);
      }
    }
  };
}

angular.module('deepsense.attributes-panel').directive('attributeNumericType', AttributeNumericType);
