'use strict';

function FlowchartBoxUndraggable() {
  return {
    restrict: 'A',
    link: function(scope, element) {
      if (scope.flowChartBoxCtrl && scope.flowChartBoxCtrl.reportMode) {
        element.on('mousedown', (event) => {
          event.preventDefault();
        });
      }
    }
  };
}

exports.inject = function(module) {
  module.directive('flowchartBoxUndraggable', FlowchartBoxUndraggable);
};
