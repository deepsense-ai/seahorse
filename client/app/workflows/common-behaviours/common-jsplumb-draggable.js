'use strict';

/* @ngInject */
function DraggableDirective(GraphNode) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      var reInitThisNodePosition = function reInitThisNodePosition () {
        scope.node.x = parseInt(element.css('left'), 10);
        scope.node.y = parseInt(element.css('top'), 10);
      };

      jsPlumb.draggable(element, {
        containment: 'parent',
        stop: () => {
          reInitThisNodePosition();
          scope.$emit(GraphNode.MOVE);
        }
      });

      scope.$on('MultipleSelection.STOP_DRAG', reInitThisNodePosition);
    }
  };
}

exports.inject = function (module) {
  module.directive('jsplumbDraggable', DraggableDirective);
};
