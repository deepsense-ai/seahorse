'use strict';

function ReplaceNullWithDefault() {
  return {
    restrict: 'A',
    link: (scope, element) => {
      element.on('blur', () => {
        let value = scope.parameter.value;
        if (_.isUndefined(value) || _.isNull(value)) {
          scope.parameter.value = scope.parameter.schema.default;
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel')
  .directive('replaceNullWithDefault', ReplaceNullWithDefault);
