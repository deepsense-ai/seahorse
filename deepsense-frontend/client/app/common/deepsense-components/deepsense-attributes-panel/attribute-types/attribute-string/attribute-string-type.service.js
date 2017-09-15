'use strict';

function StringParamTypeService() {
  this.setupStringValueBuffer = function (scope) {
    scope.valueBuffer = scope.parameter.value;
    scope.$watch('valueBuffer', function (newValue) {
      if (newValue === '') {
        scope.parameter.value = null;
      } else {
        scope.parameter.value = newValue;
      }
    });
  };
}

angular.module('deepsense.attributes-panel')
  .service('StringParamTypeService', StringParamTypeService);
