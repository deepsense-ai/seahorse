/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeStringType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-string/attribute-string-type.html',
    replace: true,
    link: function (scope) {
      StringParamTypeService.setupStringValueBuffer(scope);
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeStringType', AttributeStringType);
