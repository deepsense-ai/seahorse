'use strict';

/* @ngInject */
function FlowChartBoxController($scope, $element, GraphPanelRendererService) {
  let nodeDimensions = {};

  this.getNodeDimensions = function getNodeDimensions() {
    let $node = $('[id^="node-"]:first', $element);

    nodeDimensions.width = $node.outerWidth(true);
    nodeDimensions.height = $node.outerHeight(true);

    return nodeDimensions;
  };

  $scope.$on('StatusBar.RUN', () => this.isRunning = true);
  $scope.$on('StatusBar.ABORT', () => this.isRunning = false);
  $scope.$on('ServerCommunication.EXECUTION_FINISHED', () => this.isRunning = false);

  $scope.$on('ZOOM.ZOOM_PERFORMED', (_, data) => {
    GraphPanelRendererService.setZoom(data.zoomRatio);
  });

  $scope.$on('FIT.FIT_PERFORMED', (_, data) => {
    GraphPanelRendererService.setZoom(data.zoomRatio);
  });

  $scope.$on('Drop.EXACT', (event, dropEvent, droppedElement, droppedElementType) => {
    if (droppedElementType === 'graphNode') {
      let data = {
        dropEvent: dropEvent,
        elementId: dropEvent.dataTransfer.getData('elementId'),
        target: $($element)
          .find('.flowchart-paint-area')[0]
      };

      $scope.$emit('FlowChartBox.ELEMENT_DROPPED', data);
    }
  });
}

/* @ngInject */
function FlowChartBox($rootScope, GraphPanelRendererService) {
  return {
    restrict: 'E',
    controller: FlowChartBoxController,
    controllerAs: 'flowChartBoxCtrl',
    bindToController: true,
    replace: true,
    scope: {
      'selectedNode': '=',
      'nodes': '=',
      'reportMode': '=',
      'zoomId': '@'
    },
    templateUrl: 'app/workflows/graph-panel/graph-panel-flowchart.html',
    link: (scope, element) => {
      element.on('click', (event) => {
        if (event.target.classList.contains('flowchart-paint-area')) {
          scope.$emit('AttributePanel.UNSELECT_NODE');
        }
      });

      scope.$applyAsync(() => {
        GraphPanelRendererService.rerender();
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('flowChartBox', FlowChartBox);
};
