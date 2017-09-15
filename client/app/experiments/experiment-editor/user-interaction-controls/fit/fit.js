/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Fit() {
  return {
    restrict: 'E',
    scope: {
      text: '@',
      classSet: '@',
      icon: '@',

      relatedTo: '@',
      padding: '=',
      run: '='
    },
    replace: true,
    controller: FitController,
    controllerAs: 'fitController',
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls-element.html'
  };
}

/* @ngInject */
function FitController($document, $scope, $timeout, $rootScope, GraphPanelRendererService) {
  var that = this;
  var internal = {};

  internal.started = false;
  internal.padding = ($scope.padding / 1000) || 0;

  internal.getZoomRatioToFit = function getZoomRatioToFit (params) {
    /**
     * @description 1 means do not zoom
     * @type {{width: number, height: number}}
     */
    var zoomToFit   = {
      width:  params.visibleDimensions.width  < parseInt(params.rightmost - params.leftmost) ?
        params.visibleDimensions.width  / parseInt(params.rightmost - params.leftmost) : 1,
      height: params.visibleDimensions.height < parseInt(params.bottommost - params.topmost) ?
        params.visibleDimensions.height / parseInt(params.bottommost - params.topmost) : 1
    };
    var minZoom = Math.max(params.visibleDimensions.width / internal.element.clientWidth,
      params.visibleDimensions.height / internal.element.clientHeight);
    var zoomToBeSet = Math.min(zoomToFit.width, zoomToFit.height) - internal.padding;

    return zoomToBeSet < minZoom ? minZoom : zoomToBeSet;
  };

  internal.setFitZoom = function setFitZoom (params) {
    var ratio = internal.getZoomRatioToFit(params);

    if (ratio !== 1) {
      GraphPanelRendererService.setZoom(ratio);
    }
  };

  internal.setFit = function setFit (visibleDimensions) {
    var pseudoPosition = GraphPanelRendererService.getPseudoPosition();

    var centerOfPseudo = {
      y: ((pseudoPosition.bottommost - pseudoPosition.topmost) / 2) + pseudoPosition.topmost,
      x: ((pseudoPosition.rightmost - pseudoPosition.leftmost) / 2) + pseudoPosition.leftmost
    };

    var centerOfFlowChartBox = {
      y: internal.element.clientHeight / 2,
      x: internal.element.clientWidth / 2
    };

    var ratio = internal.getZoomRatioToFit({
      visibleDimensions,
      topmost: pseudoPosition.topmost,
      leftmost: pseudoPosition.leftmost,
      rightmost: pseudoPosition.rightmost,
      bottommost: pseudoPosition.bottommost
    });

    var newCenterOfPseudo = {
      y: ratio * centerOfPseudo.y + (1 - ratio) * centerOfFlowChartBox.y,
      x: ratio * centerOfPseudo.x + (1 - ratio) * centerOfFlowChartBox.x
    };

    var vector = {
      y: newCenterOfPseudo.y - centerOfPseudo.y,
      x: newCenterOfPseudo.x - centerOfPseudo.x
    };

    GraphPanelRendererService.setCenter({
      topmost: pseudoPosition.topmost + vector.y,
      leftmost: pseudoPosition.leftmost + vector.x,
      rightmost: pseudoPosition.rightmost + vector.x,
      bottommost: pseudoPosition.bottommost + vector.y
    });

    internal.setFitZoom({
      visibleDimensions,
      topmost: pseudoPosition.topmost,
      leftmost: pseudoPosition.leftmost,
      rightmost: pseudoPosition.rightmost,
      bottommost: pseudoPosition.bottommost
    });

    if ((
        GraphPanelRendererService.getDifferenceAfterZoom(internal.element, 'width') -
        parseInt(internal.element.style.left)
      ) < 0 || (
        GraphPanelRendererService.getDifferenceAfterZoom(internal.element, 'height') -
        parseInt(internal.element.style.top)
      ) < 0) {
      GraphPanelRendererService.setZero();
    }
  };

  internal.run = function run () {
    GraphPanelRendererService.setZero();
    GraphPanelRendererService.setZoom(1);

    var visibleDimensions = {
      width:  internal.getCurrentElementParentDimensions().width,
      height: internal.getCurrentElementParentDimensions().height
    };

    internal.setFit(visibleDimensions, GraphPanelRendererService.getAllInternalElementsPosition());
  };

  internal.start = function start () {
    if (internal.started === false) {
      internal.run();
      internal.started = true;
    }
  };

  internal.getCurrentElementDimensions = function getCurrentElementDimensions () {
    return internal.element.getBoundingClientRect();
  };

  internal.getCurrentElementParentDimensions = function getCurrentElementParentDimensions () {
    return internal.elementParent.getBoundingClientRect();
  };

  internal.triggerWidthChange = function triggerWidthChange () {
    internal.elementWidth = internal.getCurrentElementDimensions().width;
  };

  internal.init = function init () {
    internal.element            = $document[0].querySelector($scope.relatedTo);
    internal.elementParent      = internal.element.parentNode;
    internal.elementWidth       = internal.getCurrentElementDimensions().width;
    internal.elementStaticWidth   = internal.element.clientWidth;
    internal.elementStaticHeight  = internal.element.clientHeight;

    if ($scope.run) {
      $scope.$on('Experiment.RENDER_FINISHED', internal.start);
    }
  };

  $scope.force = function force (event) {
    $scope.active = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.active = false;
    }, 100);

    internal.run();
  };

  $rootScope.$on('Zoom', internal.triggerWidthChange);

  internal.init();

  return that;
}

exports.inject = function (module) {
  module.directive('dsFit', Fit);
};
