/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

var jsPlumb = require('jsPlumb');
var GraphNode = require('../common-objects/common-graph-node.js');

/* @ngInject */
function DraggableDirective() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      jsPlumb.draggable(element, {
        containment: 'parent',
        stop: () => {
          scope.node.x = parseInt(element.css('left'), 10);
          scope.node.y = parseInt(element.css('top'), 10);
          scope.$emit(GraphNode.MOVE, {});
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('jsplumbDraggable', DraggableDirective);
};
