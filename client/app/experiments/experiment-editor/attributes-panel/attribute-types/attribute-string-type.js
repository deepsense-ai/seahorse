/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeStringType() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-string-type.html',
    replace: true
  };
}

exports.inject = function (module) {
  module.directive('attributeStringType', AttributeStringType);
};
