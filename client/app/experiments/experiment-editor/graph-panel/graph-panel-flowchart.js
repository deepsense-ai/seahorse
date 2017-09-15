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
    templateUrl: 'app/experiments/experiment-editor/graph-panel/graph-panel-flowchart.html',
    link: (scope, element, attrs) => {
      element.on('click', function(event) {
        if (event.target.classList.contains('flowchart-box')) {
          scope.experiment.unselectNode();
          scope.$apply();
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('flowChartBox', FlowChartBox);
};
