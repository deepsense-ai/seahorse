'use strict';

import tpl from './attribute-load-from-library-type.html';

/*@ngInject*/
function AttributeLoadFromLibrary() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true
  };
}

angular.module('deepsense.attributes-panel')
  .directive('attributeLoadFromLibrary', AttributeLoadFromLibrary);
