'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */

/* @ngInject */
function FlowChartBoxController($scope, $element, GraphPanelRendererService) {
  let nodeDimensions = {};

  this.getNodeDimensions = function getNodeDimensions() {
    let $node = $('[id^="node-"]:first', $element);

    nodeDimensions.width = $node.outerWidth(true);
    nodeDimensions.height = $node.outerHeight(true);

    return nodeDimensions;
  };

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
function FlowChartBox(GraphPanelRendererService) {
  return {
    restrict: 'E',
    controller: FlowChartBoxController,
    controllerAs: 'flowChartBoxCtrl',
    bindToController: true,
    replace: true,
    scope: {
      'selectedNode': '=',
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

      scope.$watch('flowChartBoxCtrl.isRunning', function(newValue) {
        scope.$evalAsync(() => {
          let newRenderMode = newValue ?
            GraphPanelRendererBase.RUNNING_RENDER_MODE :
            GraphPanelRendererBase.EDITOR_RENDER_MODE;
          GraphPanelRendererService.setRenderMode(newRenderMode);
          GraphPanelRendererService.rerender();
        });
      });

      // Focus is not working properly on elements inside flowchart box.
      // It might be caused by some libraries taking over mouse event and calling preventDefault
      // This click handler manually sets focus on flowchard if anything inside it is clicked.
      $('.flowchart-box').click(function() {
        $('.flowchart-box').focus();
      });

    }
  };
}

exports.inject = function(module) {
  module.directive('flowChartBox', FlowChartBox);
};
