'use strict';

/*@ngInject*/
function AttributeBooleanType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-boolean/attribute-boolean-type.html',
    replace: true
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeBooleanType', AttributeBooleanType);
