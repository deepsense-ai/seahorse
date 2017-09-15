/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeCreatorType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-creator-type/attribute-creator-type.html',
    replace: true,
    link: function (scope) {
      StringParamTypeService.setupStringValueBuffer(scope);
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeCreatorType', AttributeCreatorType);
