/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

require('./attribute-string-type.service.js');

/*@ngInject*/
function AttributeStringType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: 'app/common/deepsense-components/deepsense-attributes-panel/attribute-types/attribute-string/attribute-string-type.html',
    replace: true,
    link: function (scope) {
      StringParamTypeService.setupStringValueBuffer(scope);
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeStringType', AttributeStringType);
