/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributesList() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attributes-list.html',
    replace: true,
    scope: {
      'parametersList': '='
    },
    link: function (scope, element, attrs) {
      scope.noParamValues = () => (Object.keys(scope.parametersList.parameters).length === 0);
    }
  };
}

exports.inject = function (module) {
  module.directive('attributesList', AttributesList);
};
