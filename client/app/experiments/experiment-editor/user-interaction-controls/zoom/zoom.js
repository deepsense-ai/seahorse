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
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls-element.html',
    link: function(scope, element, attrs, ctrl, transclude) {
      scope.value = scope.initialValue || 1;

      transclude(scope, function(clone) {
        element.append(clone);
      });
    }
  };
}

/* @ngInject */
function ZoomController($document, $scope, $timeout, GraphPanelRendererService) {
  var that = this;
  var internal = {};

  internal.max    = $scope.max;
  internal.step   = $scope.step;
  internal.relatedToElement         = $document[0].querySelector($scope.relatedTo);
  internal.viewPort                 = internal.relatedToElement.parentNode;
  internal.relatedToElementStaticWidth  = internal.relatedToElement.clientWidth;
  internal.viewPortStaticWidth          = internal.viewPort.clientWidth;
  internal.min    = internal.viewPortStaticWidth / internal.relatedToElementStaticWidth;

  internal.getMinElementZoom = function getMinElementZoom () {
    return internal.relatedToElementStaticWidth * internal.min;
  };

  internal.zoomIn = function zoomIn () {
    var result = $scope.value;

    if ($scope.value <= internal.max) {
      result = +($scope.value += internal.step).toFixed(2);
    }

    if (result >= internal.max) {
      result = $scope.value = internal.max;
    }

    return result;
  };

  internal.zoomOut = function zoomOut () {
    var result = $scope.value;

    if ($scope.value >= internal.min) {
      result = +($scope.value -= internal.step).toFixed(2);
    }

    if (result <= internal.min) {
      result = $scope.value = internal.min;
    }

    return result;
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

    var dimensionsBeforeZoom = {
      y: internal.relatedToElement.getBoundingClientRect().height,
      x: internal.relatedToElement.getBoundingClientRect().width
    };

    // GraphPanelRendererService.setCenter();
    GraphPanelRendererService.setZoom(zoom);

    var difAfterZoom = {
      y: GraphPanelRendererService.getDifferenceAfterZoom(internal.relatedToElement, 'height', dimensionsBeforeZoom.y),
      x: GraphPanelRendererService.getDifferenceAfterZoom(internal.relatedToElement, 'width', dimensionsBeforeZoom.x)
    };

    console.log(difAfterZoom);

    console.log(internal.relatedToElement.style.left);
    console.log(internal.relatedToElement.style.top);

    internal.relatedToElement.style.left = parseInt(internal.relatedToElement.style.left) - difAfterZoom.x / 2 + 'px';
    internal.relatedToElement.style.top = parseInt(internal.relatedToElement.style.top) - difAfterZoom.y / 2 + 'px';

    console.log(internal.relatedToElement.style.left);
    console.log(internal.relatedToElement.style.top);

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
    }*/

    event.preventDefault();
  };

  internal.updateState = function updateState () {
    $scope.value = jsPlumb.getZoomRatio();
  };

  internal.relatedToElement.addEventListener('wheel', internal.wheelListener);
  $scope.$on('GraphPanel.CENTERED', internal.updateState);
  $scope.$on('GraphPanel.ZERO', internal.updateState);
  $scope.$on('Zoom', internal.updateState);

  $scope.$watch('value', function (newValue) {
    GraphPanelRendererService.setZoom(newValue);
  });

  $scope.activateItem = function activateItem (event) {
    $scope.active = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.active = false;
    }, 100);

    internal.zoomOut();
  };

  $scope.activateAdditionalItem = function activateAdditionalItem (event) {
    $scope.activeAdditionalItem = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.activeAdditionalItem = false;
    }, 100);

    internal.zoomIn();
  };

  return that;
}

exports.inject = function (module) {
  module.directive('dsZoom', Zoom);
};
