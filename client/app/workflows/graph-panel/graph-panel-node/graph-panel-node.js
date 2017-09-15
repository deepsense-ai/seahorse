'use strict';

import tpl from './graph-panel-node.html';

/* @ngInject */
function GraphNodeView($rootScope, GraphNode, specialOperations, nodeTypes) {
  let sourceId = nodeTypes.CUSTOM_TRANSFORMER_SOURCE;
  let sinkId = nodeTypes.CUSTOM_TRANSFORMER_SINK;

  return {
    restrict: 'E',
    scope: {
      node: '=?',
      isSelected: '=?'
    },
    replace: true,
    templateUrl: tpl,
    controller: function($scope) {
      $scope.getNodeType = () => {
        let opId = $scope.node.operationId;
        if (opId in specialOperations) {
          return 'iconic';
        } else if (opId === sourceId || opId === sinkId) {
          return 'source-or-sink';
        } else {
          return 'plain';
        }
      };

      $scope.getIcons = () => {
        return specialOperations[$scope.node.operationId].icons;
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
