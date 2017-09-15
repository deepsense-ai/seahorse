'use strict';

/*@ngInject*/
function AttributePrefixBasedCreatorType(StringParamTypeService) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-prefix-based-creator-type/attribute-prefix-based-creator-type.html',
    replace: true,
    link: function (scope) {
      StringParamTypeService.setupStringValueBuffer(scope);

      if (!scope.parameter.value) {
        scope.valueBuffer = scope.parameter.schema.default;
      }
    }
  };
}

angular.module('deepsense.attributes-panel')
  .directive('attributePrefixBasedCreatorType', AttributePrefixBasedCreatorType);
