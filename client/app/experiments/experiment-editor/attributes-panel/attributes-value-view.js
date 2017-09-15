/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributesValueView() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attributes-value-view.html',
    replace: true,
    scope: {
      'paramName': '=',
      'paramSchemas': '=',
      'paramValues': '='
    },
    link: function (scope, element, attrs) {

    }
  };
}

exports.inject = function (module) {
  module.directive('attributesValueView', AttributesValueView);
};
