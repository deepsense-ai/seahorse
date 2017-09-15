/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeValue($compile) {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-value.html',
    replace: true
  };
}

exports.inject = function (module) {
  module.directive('attributeValue', AttributeValue);
};
