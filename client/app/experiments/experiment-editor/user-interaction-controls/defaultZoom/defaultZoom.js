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
    controller: DefaultZoomController,
    controllerAs: 'defaultZoomController',
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls-element.html'
  };
}

/* @ngInject */
function DefaultZoomController($scope, $timeout, $document, GraphPanelRendererService) {
  var that = this;
  var internal = {};

  internal.element = $document[0].querySelector($scope.relatedTo);

  $scope.force = function force (event) {
    var pseudoElement = GraphPanelRendererService.getPseudoPosition();

    $scope.active = true;
    // Show to user that it has been clicked -> user experience
    $timeout(function () {
      $scope.active = false;
    }, 100);

    GraphPanelRendererService.setCenter({
      visibleDimensions: {
        width:  jsPlumb.getContainer().parentNode.getBoundingClientRect().width,
        height: jsPlumb.getContainer().parentNode.getBoundingClientRect().height
      },
      topmost: pseudoElement.topmost,
      leftmost: pseudoElement.leftmost,
      rightmost: pseudoElement.rightmost,
      bottommost: pseudoElement.bottommost
    });
    GraphPanelRendererService.setZoom(1);

    if (
      (GraphPanelRendererService.getDifferenceAfterZoom(internal.element, 'width') -
        parseInt(internal.element.style.left)
      ) < 0 ||
      (GraphPanelRendererService.getDifferenceAfterZoom(internal.element, 'height') -
        parseInt(internal.element.style.top)
      ) < 0
    ) {
      GraphPanelRendererService.setZero();
    }
  };

  return that;
}

exports.inject = function (module) {
  module.directive('dsDefaultZoom', DefaultZoom);
};
