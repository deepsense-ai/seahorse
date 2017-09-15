/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function FlowChartBox() {
  return {
    restrict: 'E',
    scope: false,
    replace: true,
    templateUrl: 'app/experiments/experiment.flowChartBox.html',
    link: function (scope, element, attrs) {
      element.on('click', function(event) {
        if (event.target.classList.contains('flowchart-box')) {
          scope.experiment.showOperationAttributesPanel.value = false;
          scope.$apply();
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('flowChartBox', FlowChartBox);
};
