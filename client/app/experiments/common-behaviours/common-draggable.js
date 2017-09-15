/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

var GraphNode = require('../common-objects/common-graph-node.js');

/* @ngInject */
function DraggableDirective() {
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
          scope.$emit(GraphNode.MOVE, {});
        }
      });

      scope.$on('MultipleSelection.STOP_DRAG', reInitThisNodePosition);
    }
  };
}

exports.inject = function (module) {
  module.directive('jsplumbDraggable', DraggableDirective);
};
