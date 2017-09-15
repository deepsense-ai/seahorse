/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributePrefixBasedCreatorType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: 'app/common/deepsense-components/deepsense-attributes-panel/attribute-types/attribute-prefix-based-creator-type/attribute-prefix-based-creator-type.html',
    replace: true,
    link: function (scope) {
      StringParamTypeService.setupStringValueBuffer(scope);
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributePrefixBasedCreatorType', AttributePrefixBasedCreatorType);
