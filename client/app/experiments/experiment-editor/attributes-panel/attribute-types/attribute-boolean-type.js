/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeBooleanType() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-boolean-type.html',
    replace: true
  };
}

exports.inject = function (module) {
  module.directive('attributeBooleanType', AttributeBooleanType);
};
