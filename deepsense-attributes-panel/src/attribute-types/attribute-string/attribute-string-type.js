/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeStringType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-string/attribute-string-type.html',
    replace: true
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeStringType', AttributeStringType);
