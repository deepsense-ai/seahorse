'use strict';

/* @ngInject */
function FlowChartBox() {
  return {
    restrict: 'E',
    controller: 'FlowChartBoxController',
    controllerAs: 'flowChartBoxCtrl',
    bindToController: true,
    replace: true,
    scope: {
      'selectedNode': '=',
      'workflow': '=',
      'nodes': '=',
      'isRunning': '=',
      'zoomId': '@'
    },
    templateUrl: 'app/workflows/graph-panel/graph-panel-flowchart.html',
    link: (scope, element) => {
      element.on('click', (event) => {
        if (event.target.classList.contains('flowchart-paint-area')) {
          scope.$emit('AttributePanel.UNSELECT_NODE');
        }
      });

      // Focus is not working properly on elements inside flowchart box.
      // It might be caused by some libraries taking over mouse event and calling preventDefault
      // This click handler manually sets focus on flowchart if anything inside it is clicked.
      $('.flowchart-box').click(function() {
        $('.flowchart-box').focus();
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('flowChartBox', FlowChartBox);
};
