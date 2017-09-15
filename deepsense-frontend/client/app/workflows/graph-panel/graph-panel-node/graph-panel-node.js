'use strict';

/*@ngInject*/
function GraphNodeView($rootScope, GraphNode) {
  return {
    restrict: 'E',
    scope: {
      node: '=?',
      isSelected: '=?'
    },
    replace: true,
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/graph-panel-node.html',
    controller: function($scope) {
      $scope.$watch('node.knowledgeErrors', () => {
        $scope.getTooltipMessage = function() {
          var errors = $scope.node.getFancyKnowledgeErrors();
          return errors ? errors : '';
        };
      });

      $scope.isSourceOrSink = () => {
        let sourceId = 'f94b04d7-ec34-42f7-8100-93fe235c89f8';
        let sinkId = 'e652238f-7415-4da6-95c6-ee33808561b2';
        return $scope.node.operationId === sourceId || $scope.node.operationId === sinkId;
      };

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
