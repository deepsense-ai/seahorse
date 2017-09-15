/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function GraphNode(UPDATE_CLICKED_NODE) {
  return {
    restrict: 'E',
    scope: {
      node: '='
    },
    replace: true,
    templateUrl: 'app/experiments/experiment-editor/graph-panel/graph-panel-node.html',
    link: function (scope, element, attrs) {
      element.on('click', function () {
        scope.$emit(UPDATE_CLICKED_NODE, {
          selectedNodeId: scope.node.id
        });
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('graphNode', GraphNode);
};
