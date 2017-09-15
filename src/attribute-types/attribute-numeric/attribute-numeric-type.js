/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeNumericType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-numeric/attribute-numeric-type.html',
    replace: true,
    link: function(scope, element) {
      let validator = scope.parameter.validator;
      if (validator && validator.schema && validator.schema.type === 'range') {
        element[0].setAttribute('min', validator.schema.configuration.begin);
        element[0].setAttribute('max', validator.schema.configuration.end);
        element[0].setAttribute('step', validator.schema.configuration.step);
      }
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeNumericType', AttributeNumericType);
