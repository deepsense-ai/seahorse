'use strict';

/*@ngInject*/
function GraphNodeView($rootScope, $sce, GraphNode) {
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
      element.on('click', function($event) {
        $rootScope.$broadcast('GraphNode.CLICK', {
          originalEvent: $event,
          selectedNode: scope.node
        });
      });

      element.on('mousedown', function($event) {
        $rootScope.$broadcast('GraphNode.MOUSEDOWN', {
          originalEvent: $event,
          selectedNode: scope.node
        });
      });

      element.on('mouseup', function($event) {
        $rootScope.$broadcast('GraphNode.MOUSEUP', {
          originalEvent: $event,
          selectedNode: scope.node
        });
      });

      element.on('mousemove', function($event) {
        $rootScope.$broadcast('GraphNode.MOUSEMOVE', {
          originalEvent: $event,
          selectedNode: scope.node
        });
      });

    }
  };
}

exports.inject = function(module) {
  module.directive('graphNode', GraphNodeView);
};
