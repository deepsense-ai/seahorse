/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var jsPlumb = require('jsPlumb');

function Zoom() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      text: '@',
      classSet: '@', // TODO check why does not work
      icon: '@',
      max: '=',
      step: '=',
      initialValue: '=',  // TODO check why does not work
      relatedTo: '@'
    },
    transclude: true,
    controller: ZoomController,
    controllerAs: 'zoomController',
    templateUrl: 'app/experiments/experiment-editor/graph-panel/user-interaction-controls/user-interaction-controls-element.html',
    link: function(scope, element, attrs, ctrl, transclude) {
      scope.value = scope.initialValue || 1;

      transclude(scope, function(clone) {
        element.append(clone);
      });
    }
  };
}

/* @ngInject */
function ZoomController($document, $scope, $timeout, GraphPanelRendererService, MouseEvent) {
  var that = this;
  var internal = {};

  internal.BORDER_SHIFT = 3;

  internal.max    = $scope.max;
  internal.step   = $scope.step;
  internal.relatedToElement         = $document[0].querySelector($scope.relatedTo);
  internal.viewPort                 = internal.relatedToElement.parentNode;
  internal.relatedToElementStaticWidth  = internal.relatedToElement.clientWidth;
  internal.relatedToElementStaticHeight = internal.relatedToElement.clientHeight;
  internal.viewPortStaticWidth          = internal.viewPort.clientWidth;
  internal.min    = 0.4; // internal.viewPortStaticWidth / internal.relatedToElementStaticWidth

  // TODO change $scope.value behaviour in order to have only one executing

  internal.zoomIn = function zoomIn () {
    var result = $scope.value;

    if (result <= internal.max) {
      result = +(result + internal.step).toFixed(2);
    }

    if (result >= internal.max) {
      result = internal.max;
    }

    return result;
  };

  internal.zoomOut = function zoomOut () {
    var result = $scope.value;

    if (result >= internal.min) {
      result = +(result - internal.step).toFixed(2);
    }

    if (result <= internal.min) {
      result = internal.min;
    }

    return result;
  };

  var old = {
    x: 0,
    y: 0
  };

  internal.wheelListener = function wheelListener (event) {
    var userWheelData = event.deltaY;
    var zoom;

    // zoom out wheel is rolling down
    if (userWheelData > 0) {
      zoom = internal.zoomOut();
    // zoom in wheel is rolling up
    } else {
      zoom = internal.zoomIn();
    }

    event.preventDefault();

    if (zoom === internal.min || zoom === internal.max) {
      return false;
    }

    $scope.value = zoom;

    var eventOccurredIn = MouseEvent.getEventOffsetOfElement(event, internal.relatedToElement);
    console.log(old);
    console.log(eventOccurredIn.x, eventOccurredIn.y);
    eventOccurredIn.y = eventOccurredIn.y - internal.BORDER_SHIFT;

    var dif = {
      x: 0,
      y: 0
    };

    if (old.x && old.y && (eventOccurredIn.x - old.x > 50 || eventOccurredIn.y - old.y > 50)) {
      dif = {
        x: eventOccurredIn.x - old.x,
        y: eventOccurredIn.y - old.y
      };
    }

    var result = {
      x: dif.x * (zoom),
      y: dif.y * (zoom)
    };

    old = eventOccurredIn;

    console.log(dif);

    GraphPanelRendererService.setZoom(
      zoom,

      [
        eventOccurredIn.x,
        eventOccurredIn.y
      ],

      result
    );

    console.log(zoom);

    /*if (
      GraphPanelRendererService.getDifferenceAfterZoom(internal.relatedToElement, 'width') -
      parseInt(internal.relatedToElement.style.left) < 0
    ) {
      GraphPanelRendererService.setZero('left');
    }

    if (
      GraphPanelRendererService.getDifferenceAfterZoom(internal.relatedToElement, 'height') -
      parseInt(internal.relatedToElement.style.top) < 0
    ) {
      GraphPanelRendererService.setZero('top');
    });*/
  };

  internal.updateState = function updateState () {
     $scope.value = jsPlumb.getZoom();
  };

  internal.relatedToElement.addEventListener('wheel', internal.wheelListener);
  /*$scope.$on('GraphPanel.CENTERED', internal.updateState);
  $scope.$on('GraphPanel.ZERO', internal.updateState);
  $scope.$on('Zoom', internal.updateState);*/

  // TODO normal two way binding
  /*$scope.$watch('rangeValue', function (newValue) {
    var pseudoCenter = GraphPanelRendererService.getPseudoContainerCenter();

    $scope.value = newValue;

    GraphPanelRendererService.setZoom($scope.value, [pseudoCenter.x / internal.relatedToElementStaticWidth, pseudoCenter.y / internal.relatedToElementStaticHeight]);
  });*/

  /*$scope.activateItem = function activateItem (event) {
    var pseudoCenter = GraphPanelRendererService.getPseudoContainerCenter();

    $scope.active = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.active = false;
    }, 100);

    $scope.value = internal.zoomOut();

    GraphPanelRendererService.setZoom($scope.value, [pseudoCenter.x / internal.relatedToElementStaticWidth, pseudoCenter.y / internal.relatedToElementStaticHeight]);
  };

  $scope.activateAdditionalItem = function activateAdditionalItem (event) {
    var pseudoCenter = GraphPanelRendererService.getPseudoContainerCenter();

    $scope.activeAdditionalItem = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.activeAdditionalItem = false;
    }, 100);

    $scope.value = internal.zoomIn();

    GraphPanelRendererService.setZoom($scope.value, [pseudoCenter.x / internal.relatedToElementStaticWidth, pseudoCenter.y / internal.relatedToElementStaticHeight]);
  };*/

  return that;
}

exports.inject = function (module) {
  module.directive('dsZoom', Zoom);
};
