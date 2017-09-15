/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeSnippetType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-snippet/attribute-snippet-type.html',
    replace: true
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeSnippetType', AttributeSnippetType);
