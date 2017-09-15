'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';
/* beautify preserve:end */

/* @ngInject */
function FlowChartBoxController($scope, $element, $timeout, GraphPanelRendererService) {
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

  $scope.$watch('isRunning', function(newValue) {
    $timeout(() => {
      let newRenderMode = newValue ?
        GraphPanelRendererBase.RUNNING_RENDER_MODE :
        GraphPanelRendererBase.EDITOR_RENDER_MODE;
      GraphPanelRendererService.setRenderMode(newRenderMode);
      GraphPanelRendererService.rerender($scope.flowChartBoxCtrl.workflow);
    }, 0);
  });

  jsPlumb.bind('connectionDragStop', () => GraphPanelRendererService.disablePortHighlightings(this.workflow));

}

exports.inject = function(module) {
  module.controller('FlowChartBoxController', FlowChartBoxController);
};