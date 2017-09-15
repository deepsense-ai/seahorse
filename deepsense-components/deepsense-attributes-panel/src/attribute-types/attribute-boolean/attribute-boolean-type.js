'use strict';

/*@ngInject*/
function AttributeBooleanType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-boolean/attribute-boolean-type.html',
    replace: true,
    link: function (scope) {
      scope.valueBuffer = scope.parameter.getValueOrDefault();
      scope.$watch('valueBuffer', function (newValue) {
        if (newValue !== scope.parameter.getValueOrDefault()) {
          scope.parameter.value = newValue;
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeBooleanType', AttributeBooleanType);
