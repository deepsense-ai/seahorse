/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

/* @ngInject */
function DraggableDirective() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      jsPlumb.draggable(element, {
        containment: 'parent'
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('jsplumbDraggable', DraggableDirective);
};
