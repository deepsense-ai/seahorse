/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeSnippetType() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-snippet-type.html',
    replace: true
  };
}

exports.inject = function (module) {
  module.directive('attributeSnippetType', AttributeSnippetType);
};
