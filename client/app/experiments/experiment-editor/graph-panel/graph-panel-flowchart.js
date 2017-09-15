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
    replace: true,
    controllerAs: 'flowChartBoxController',
    templateUrl: 'app/experiments/experiment-editor/graph-panel/graph-panel-flowchart.html',
    link: (scope, element, attrs, controller) => {
      element.on('click', function (event) {
        if (event.target.classList.contains('flowchart-box')) {
          scope.experiment.unselectNode();
          scope.$apply();
        }
      });

      element.on('contextmenu',(event) => {
        event.preventDefault();
      });
    }
  };
}

function FlowChartBoxController($scope, $element, $window) {
  var internals = {};

  internals.closeContextMenu = function closeContextMenu () {
    $scope.contextMenuController.setInvisible();
    $scope.$digest();
  };

  internals.checkClosing = function checkClosing (event) {
    return event.target && event.target.matches('.context-menu *') === false;
  };

  internals.checkAndClose = function checkAndClose (event) {
    if (internals.checkClosing(event)) {
      internals.closeContextMenu();
    }
  };

  $scope.$on('OutputPoint.CONTEXTMENU', function (customEvent, endPointData) {
    var event = endPointData.event;

    event.preventDefault();

    $scope.contextMenuController.setPosition({
      x: $element[0].offsetLeft,
      y: $element[0].offsetTop
    }, {
      x: event.pageX,
      y: event.pageY
    });

    $scope.contextMenuController.setVisible();
    $scope.$digest();

    return false;
  });

  $scope.$on(GraphNode.MOUSEDOWN, internals.closeContextMenu);
  $scope.$on(Edge.DRAG, internals.closeContextMenu);
  $scope.$on('OutputPoint.CLICK', internals.closeContextMenu);
  $scope.$on('InputPoint.CLICK', internals.closeContextMenu);

  $window.addEventListener('mousedown', internals.checkAndClose);
  $window.addEventListener('blur', internals.closeContextMenu);

  $scope.$on('$destroy', () => {
    $window.removeEventListener('mousedown', internals.checkAndClose);
    $window.removeEventListener('blur', internals.closeContextMenu);
  });
}

exports.inject = function (module) {
  module.directive('flowChartBox', FlowChartBox);
};
