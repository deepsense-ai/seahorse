/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

import tpl from './attribute-string-type.html';

require('./attribute-string-type.service.js');

/* @ngInject */
function AttributeStringType(StringParamTypeService) {
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
  .directive('attributeStringType', AttributeStringType);
