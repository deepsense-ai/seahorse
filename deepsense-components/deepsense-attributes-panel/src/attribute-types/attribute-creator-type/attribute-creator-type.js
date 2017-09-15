'use strict';

/*@ngInject*/
function AttributeCreatorType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-creator-type/attribute-creator-type.html',
    replace: true,
    link: function (scope) {

      if (_.isUndefined(scope.parameter.value) || _.isNull(scope.parameter.value)) {
        scope.parameter.value = scope.parameter.defaultValue;
      }

      StringParamTypeService.setupStringValueBuffer(scope);
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeCreatorType', AttributeCreatorType);
