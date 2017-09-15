/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

var GraphNode = require('../../common-objects/common-graph-node.js');
var Edge = require('../../common-objects/common-edge.js');

/* @ngInject */
function FlowChartBox() {
  return {
    restrict: 'E',
    controller: FlowChartBoxController,
    controllerAs: 'flowChartBoxController',
    replace: true,
    scope: true,
    templateUrl: 'app/experiments/experiment-editor/graph-panel/graph-panel-flowchart.html',
    link: (scope, element) => {
      element.on('click', function (event) {
        if (event.target.classList.contains('flowchart-paint-area')) {
          scope.experiment.unselectNode();
          scope.$apply();
        }
      });
    }
  };
}

/* @ngInject */
function FlowChartBoxController($scope, $element, $window,
                                ExperimentService, ReportOptionsService,
                                GraphPanelRendererService, DeployModelService,
                                MouseEvent) {
  var that = this;
  var internal = {};

  internal.contextMenuState = 'invisible';
  internal.contextMenuPosition = {};

  internal.closeContextMenu = function closeContextMenu () {
    $scope.$broadcast('ContextMenu.CLOSE');
    internal.contextMenuState = 'invisible';
    $scope.$digest();
  };

  internal.contextMenuOpener = function contextMenuOpener (event, data) {
    let portEl = data.reference.canvas;
    let dimensions = portEl.getBoundingClientRect();
    const TOP_SHIFT = 10;

    internal.contextMenuPosition.x = dimensions.left + dimensions.width * jsPlumb.getZoom();
    internal.contextMenuPosition.y = $(portEl).offset().top + TOP_SHIFT;
    internal.contextMenuState = 'visible';
    $scope.$digest();
  };

  internal.isNotInternal = function isNotInternal (event) {
    return event.target && event.target.matches('.context-menu *') === false;
  };

  internal.checkClickAndClose = function checkClickAndClose (event) {
    if (internal.isNotInternal(event)) {
      internal.closeContextMenu();
    }
  };

  internal.handlePortRightClick = function handlePortRightClick (event, data) {
    let port = data.reference;
    let nodeId = port.getParameter('nodeId');
    let currentNode = ExperimentService.getExperiment().getNodes()[nodeId];

    ReportOptionsService.setCurrentPort(port);
    ReportOptionsService.setCurrentNode(currentNode);
    ReportOptionsService.clearReportOptions();
    ReportOptionsService.updateReportOptions();

    internal.contextMenuOpener.apply(internal, arguments);
  };

  that.getContextMenuState = function getContextMenuState () {
    return internal.contextMenuState;
  };

  that.getContextMenuPositionY = function getContextMenuPositionY() {
    return internal.contextMenuPosition.y;
  };

  that.getContextMenuPositionX = function getContextMenuPositionX() {
    return internal.contextMenuPosition.x;
  };

  that.getPositionY = function getPositionY() {
    return $element[0].offsetTop;
  };

  that.getPositionX = function getPositionX() {
    return $element[0].getBoundingClientRect().left;
  };

  that.getReportOptions = function getReportOptions() {
    return ReportOptionsService.getReportOptions();
  };

  $scope.$on(GraphNode.MOUSEDOWN, internal.closeContextMenu);
  $scope.$on(Edge.DRAG, internal.closeContextMenu);
  $scope.$on('InputPoint.CLICK', internal.closeContextMenu);
  $scope.$on('OutputPort.LEFT_CLICK', internal.closeContextMenu);
  $scope.$on('OutputPort.RIGHT_CLICK', internal.handlePortRightClick);
  $scope.$on('Keyboard.KEY_PRESSED_ESC', internal.closeContextMenu);

  $window.addEventListener('mousedown', internal.checkClickAndClose);
  $window.addEventListener('blur', internal.closeContextMenu);

  $scope.$on('ZOOM.ZOOM_PERFORMED', (event, data) => {
    GraphPanelRendererService.setZoom(data.zoomRatio);
  });

  $scope.$on('Model.DEPLOY', (event, data) => {
    DeployModelService.showModal(data);
  });

  $scope.$on('Drag.START', (event, dragEvent, dragEventElement) => {

  });

  $scope.$on('Drop.EXACT', (event, dropEvent, droppedElement, droppedElementType) => {
    if (droppedElementType === 'graphNode') {
      let data = {};

      data.dropEvent = dropEvent;
      data.elementId = dropEvent.dataTransfer.getData('elementId');
      data.target = dropEvent.target;

      $scope.$emit('FlowChartBox.ELEMENT_DROPPED', data);
    }
  });

  $scope.$on('$destroy', () => {
    $window.removeEventListener('mousedown', internal.checkClickAndClose);
    $window.removeEventListener('blur', internal.closeContextMenu);
  });
}

exports.inject = function (module) {
  module.directive('flowChartBox', FlowChartBox);
};
