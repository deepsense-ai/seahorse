/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

import tpl from './attribute-prefix-based-creator-type.html';

/* @ngInject */
function AttributePrefixBasedCreatorType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    link: function (scope) {
      StringParamTypeService.setupStringValueBuffer(scope);
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributePrefixBasedCreatorType', AttributePrefixBasedCreatorType);
