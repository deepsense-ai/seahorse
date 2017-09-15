/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function GraphNodeView(GraphNode) {
  return {
    restrict: 'E',
    scope: {
      node: '='
    },
    replace: true,
    templateUrl: 'app/workflows/workflows-editor/graph-panel/graph-panel-node/graph-panel-node.html',
    link: function (scope, element, attrs) {
      element.on('click', function () {
        scope.$emit(GraphNode.CLICK, {
          selectedNode: scope.node
        });
      });

      element.on('mousedown', function () {
        scope.$emit(GraphNode.MOUSEDOWN, {
          selectedNode: scope.node
        });
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('graphNode', GraphNodeView);
};
