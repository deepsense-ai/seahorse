'use strict';

/*@ngInject*/
function AttributeLoadFromLibrary() {
  return {
    restrict: 'E',
    templateUrl: 'app/common/deepsense-components/deepsense-attributes-panel/attribute-types/attribute-load-from-library/attribute-load-from-library-type.html',
    replace: true
  };
}

angular.module('deepsense.attributes-panel')
  .directive('attributeLoadFromLibrary', AttributeLoadFromLibrary);
