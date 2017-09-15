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
    transclude: true,
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
    var minZoom = Math.max(params.visibleDimensions.width / internal.relatedToElement.clientWidth,
      params.visibleDimensions.height / internal.relatedToElement.clientHeight);
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
    var pseudoPosition = GraphPanelRendererService.getPseudoContainerPosition();
    var centerOfPseudo = GraphPanelRendererService.getPseudoContainerCenter();

    var centerOfFlowChartBox = {
      y: internal.relatedToElement.clientHeight / 2,
      x: internal.relatedToElement.clientWidth / 2
    };

    var ratio = internal.getZoomRatioToFit({
      visibleDimensions,
      topmost: pseudoPosition.topmost,
      leftmost: pseudoPosition.leftmost,
      rightmost: pseudoPosition.rightmost,
      bottommost: pseudoPosition.bottommost
    });

    var newCenterOfPseudo =
      GraphPanelRendererService.getNewCenterOf(ratio, centerOfFlowChartBox, centerOfPseudo);

    var vector = {
      y: newCenterOfPseudo.y - centerOfPseudo.y,
      x: newCenterOfPseudo.x - centerOfPseudo.x
    };

    var resultCenter = {
      y: centerOfPseudo.y + vector.y,
      x: centerOfPseudo.x + vector.x
    };

    console.log('FIT CENTER TO: ', resultCenter);

    GraphPanelRendererService.setCenter(resultCenter);

    internal.setFitZoom({
      visibleDimensions,
      topmost: pseudoPosition.topmost,
      leftmost: pseudoPosition.leftmost,
      rightmost: pseudoPosition.rightmost,
      bottommost: pseudoPosition.bottommost
    });

    if (
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
    }
  };

  internal.run = function run () {
    GraphPanelRendererService.setZero('top');
    GraphPanelRendererService.setZero('left');
    GraphPanelRendererService.setZoom(1);

    var visibleDimensions = {
      width:  internal.getViewPortDimensions().width,
      height: internal.getViewPortDimensions().height
    };

    internal.setFit(visibleDimensions);
  };

  internal.start = function start () {
    if (internal.started === false) {
      internal.run();
      internal.started = true;
    }
  };

  internal.getCurrentElementDimensions = function getCurrentElementDimensions () {
    return internal.relatedToElement.getBoundingClientRect();
  };

  internal.getViewPortDimensions = function getViewPortDimensions () {
    return internal.viewPort.getBoundingClientRect();
  };

  internal.init = function init () {
    internal.relatedToElement             = $document[0].querySelector($scope.relatedTo);
    internal.viewPort                     = internal.relatedToElement.parentNode;
    internal.relatedToElementWidth        = internal.getCurrentElementDimensions().width;
    internal.relatedToElementStaticWidth  = internal.relatedToElement.clientWidth;
    internal.relatedToElementStaticHeight = internal.relatedToElement.clientHeight;

    if ($scope.run) {
      $scope.$on('Experiment.RENDER_FINISHED', internal.start);
    }
  };

  $scope.activateItem = function activateItem (event) {
    $scope.active = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.active = false;
    }, 100);

    internal.run();
  };

  internal.init();

  return that;
}

exports.inject = function (module) {
  module.directive('dsFit', Fit);
};
