/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function DefaultZoom() {
  return {
    restrict: 'E',
    scope: {
      text: '@',
      classSet: '@',
      icon: '@',

      relatedTo: '@'
    },
    replace: true,
    transclude: true,
    controller: DefaultZoomController,
    controllerAs: 'defaultZoomController',
    templateUrl: 'app/experiments/experiment-editor/graph-panel/user-interaction-controls/user-interaction-controls-element.html'
  };
}

/* @ngInject */
function DefaultZoomController($scope, $timeout, $document, GraphPanelRendererService) {
  var that = this;
  var internal = {};

  internal.relatedToElement = $document[0].querySelector($scope.relatedTo);

  $scope.activateItem = function activateItem (event) {
    $scope.active = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.active = false;
    }, 100);

    GraphPanelRendererService.setZoom(1);
    GraphPanelRendererService.setCenter(GraphPanelRendererService.getPseudoContainerCenter());

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

  return that;
}

exports.inject = function (module) {
  module.directive('dsDefaultZoom', DefaultZoom);
};
