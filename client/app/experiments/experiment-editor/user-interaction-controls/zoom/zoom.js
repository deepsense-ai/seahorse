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
      elementToZoom: '@'
    },
    controller: ZoomController,
    controllerAs: 'zoomController',
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls-element.html'
  };
}

/* @ngInject */
function ZoomController($document, $scope, GraphPanelRendererService, $rootScope) {
  var that = this;
  var internal = {};

  internal.max    = $scope.max;
  internal.step   = $scope.step;
  internal.value  = $scope.initialValue || 1;
  internal.elementToZoom            = $document[0].querySelector($scope.elementToZoom);
  internal.elementToZoomParent      = internal.elementToZoom.parentNode;
  internal.elementStaticWidth       = internal.elementToZoom.clientWidth;
  internal.elementParentStaticWidth = internal.elementToZoomParent.clientWidth;
  internal.min    = internal.elementParentStaticWidth / internal.elementStaticWidth;

  console.log(internal.min, internal.value);

  internal.getMinElementZoom = function getMinElementZoom () {
    return internal.elementStaticWidth * internal.min;
  };

  internal.zoomIn = function zoomIn () {
    var result = internal.value;

    if (internal.value <= internal.max) {
      result = +(internal.value += internal.step).toFixed(2);
    }

    if (result >= internal.max) {
      result = internal.value = internal.max;
    }

    return result;
  };

  internal.zoomOut = function zoomOut () {
    var result = internal.value;

    if (internal.value >= internal.min) {
      result = +(internal.value -= internal.step).toFixed(2);
    }

    if (result <= internal.min) {
      result = internal.value = internal.min;
    }

    return result;
  };

  internal.handleMinZoom = function handleMinZoom (zoom) {
    // TODO make it work with any value ... because now it is buggy.
    $rootScope.$broadcast('Zoom.MIN_REACHED', {
      stepAmount : internal.elementStaticWidth * internal.step * 2
    });
  };

  internal.wheelListener = function wheelListener (event) {
    var userWheelData = event.wheelDelta;
    var zoom;

    // zoom out wheel is rolling down
    if (userWheelData < 0) {
      zoom = internal.zoomOut();
    // zoom in wheel is rolling up
    } else {
      zoom = internal.zoomIn();
    }

    if (zoom === internal.min && jsPlumb.getZoom() > zoom) {
      internal.handleMinZoom(zoom);
    }

    GraphPanelRendererService.setZoom(zoom);

    if ((
      GraphPanelRendererService.getDifferenceAfterZoom(internal.elementToZoom, 'width') -
      parseInt(internal.elementToZoom.style.left)
      ) < 0 || (
      GraphPanelRendererService.getDifferenceAfterZoom(internal.elementToZoom, 'height') -
      parseInt(internal.elementToZoom.style.top)
      ) < 0) {
      GraphPanelRendererService.setZero();
    }

    event.preventDefault();
  };

  internal.updateState = function updateState () {
    internal.value = jsPlumb.getZoom();
  };

  internal.elementToZoom.addEventListener('wheel', internal.wheelListener);
  $rootScope.$on('GraphPanel.CENTERED', internal.updateState);
  $rootScope.$on('GraphPanel.ZERO', internal.updateState);
  $rootScope.$on('Zoom', internal.updateState);

  return that;
}

exports.inject = function (module) {
  module.directive('dsZoom', Zoom);
};
