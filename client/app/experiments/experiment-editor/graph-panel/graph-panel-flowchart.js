/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

var GraphNode = require('../../common-objects/common-graph-node.js');
var Edge = require('../../common-objects/common-edge.js');

function FlowChartBox() {
  return {
    restrict: 'E',
    controller: FlowChartBoxController,
    controllerAs: 'flowChartBoxController',
    replace: true,
    templateUrl: 'app/experiments/experiment-editor/graph-panel/graph-panel-flowchart.html',
    link: (scope, element) => {
      element.on('click', function (event) {
        if (event.target.classList.contains('flowchart-box')) {
          scope.experiment.unselectNode();
          scope.$apply();
        }
      });
    }
  };
}

function FlowChartBoxController($scope, $element, $window) {
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
    internal.contextMenuPosition.x = data.event.pageX;
    internal.contextMenuPosition.y = data.event.pageY;
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

  internal.checkKeyAndClose = function checkKeyAndClose (event) {
    if (event.keyCode === 27) {
      internal.closeContextMenu();
    }
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
    return $element[0].getBoundingClientRect().top;
  };

  that.getPositionX = function getPositionX() {
    return $element[0].getBoundingClientRect().left;
  };

  $scope.$on(GraphNode.MOUSEDOWN, internal.closeContextMenu);
  $scope.$on(Edge.DRAG, internal.closeContextMenu);
  $scope.$on('InputPoint.CLICK', internal.closeContextMenu);
  $scope.$on('OutputPort.LEFT_CLICK', internal.closeContextMenu);
  $scope.$on('ReportOptions.UPDATED', internal.contextMenuOpener);

  $window.addEventListener('mousedown', internal.checkClickAndClose);
  $window.addEventListener('keydown', internal.checkKeyAndClose);
  $window.addEventListener('blur', internal.closeContextMenu);

  $scope.$on('$destroy', () => {
    $window.removeEventListener('mousedown', internal.checkClickAndClose);
    $window.removeEventListener('keydown', internal.checkKeyAndClose);
    $window.removeEventListener('blur', internal.closeContextMenu);
  });
}

exports.inject = function (module) {
  module.directive('flowChartBox', FlowChartBox);
};
