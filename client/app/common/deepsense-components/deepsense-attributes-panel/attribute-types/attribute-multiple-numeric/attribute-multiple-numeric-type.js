'use strict';

import tpl from './attribute-multiple-numeric-type.html';

/*@ngInject*/
function AttributeMultipleNumericType() {
    return {
        restrict: 'E',
        templateUrl: tpl,
        replace: true,
        link: function(scope) {
            scope.rawValue = scope.parameter.rawValue();
            scope.$watch('rawValue', function(newValue) {
                let value = scope.parameter.parseRawValue(newValue);
                if (scope.parameter.validateValue(value)) {
                  scope.parameter.value = value;
                } else {
                  scope.parameter.value = null;
                }
            });
        }
    };
}

angular.module('deepsense.attributes-panel').
directive('attributeMultipleNumericType', AttributeMultipleNumericType);
