/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeNumericType() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-numeric-type.html',
    replace: true,
    link: function(scope, element) {
      let validator = scope.parameter.schema.validator;
      if (validator && validator.schema.type === 'range') {
        element[0].setAttribute('min', validator.schema.configuration.begin);
        element[0].setAttribute('max', validator.schema.configuration.end);
        element[0].setAttribute('step', validator.schema.configuration.step);
      }
    }
  };
}

exports.inject = function (module) {
  module.directive('attributeNumericType', AttributeNumericType);
};
