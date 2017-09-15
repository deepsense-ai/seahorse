'use strict';

/*@ngInject*/
function GraphNodeView($rootScope, GraphNode) {

  let sourceId = 'f94b04d7-ec34-42f7-8100-93fe235c89f8';
  let sinkId = 'e652238f-7415-4da6-95c6-ee33808561b2';

  let specialNodes = {
    // Notebook
    'e76ca616-0322-47a5-b390-70c9668265dd': {
      icons: ['fa fa-book']
    },
    // Evaluate
    'a88eaf35-9061-4714-b042-ddd2049ce917': {
      icons: ['fa fa-tachometer']
    },
    // Fit
    '0c2ff818-977b-11e5-8994-feff819cdc9f': {
      icons: ['fa fa-gears']
    },
    // Transform
    '643d8706-24db-4674-b5b4-10b5129251fc': {
      icons: ['fa fa-bolt']
    },
    // Fit + Transform
    '1cb153f1-3731-4046-a29b-5ad64fde093f': {
      icons: [
        'fa fa-gears',
        'fa fa-bolt'
      ]
    }
  };

  return {
    restrict: 'E',
    scope: {
      node: '=?',
      isSelected: '=?'
    },
    replace: true,
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/graph-panel-node.html',
    controller: function($scope) {
      $scope.getNodeType = () => {
        let opId = $scope.node.operationId;
        if (opId in specialNodes) {
          return 'iconic';
        } else if (opId === sourceId || opId === sinkId) {
          return 'source-or-sink';
        } else {
          return 'plain';
        }
      };

      $scope.getIcons = () => {
        return specialNodes[$scope.node.operationId].icons;
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
