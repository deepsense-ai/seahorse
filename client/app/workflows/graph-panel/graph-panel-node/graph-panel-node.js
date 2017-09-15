'use strict';

/*@ngInject*/
function GraphNodeView($sce, GraphNode) {
  return {
    restrict: 'E',
    scope: {
      node: '='
    },
    replace: true,
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/graph-panel-node.html',
    controller: function($scope) {
      $scope.$watch('node.knowledgeErrors', () => {
        $scope.tooltipMessage = $sce.trustAsHtml(`
          <div class="text-left--important">
            ${$scope.node.getFancyKnowledgeErrors()}
          </div>
        `);
      });
    },
    link: function(scope, element) {
      element.on('click', function() {
        scope.$emit(GraphNode.CLICK, {
          selectedNode: scope.node
        });
      });

      element.on('mousedown', function() {
        scope.$emit(GraphNode.MOUSEDOWN, {
          selectedNode: scope.node
        });
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('graphNode', GraphNodeView);
};
