'use strict';

import tpl from './attribute-boolean-type.html';

/* @ngInject */
function AttributeBooleanType() {
  return {
    restrict: 'E',
    templateUrl: tpl,
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

angular
  .module('deepsense.attributes-panel')
  .directive('attributeBooleanType', AttributeBooleanType);
