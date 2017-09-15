/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

var internal = {};

/* @ngInject */
function DropTarget() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      internal.scope = scope;
      internal.element = element;
      internal.attrs = attrs;

      element.on('dragover', function dragOver(event) {
        event.preventDefault();
        return true;
      });

      element.on('drop', function dragEnd(event) {
        if (event.dataTransfer.getData('droppable') === 'true') {
          let data = {};
          data.dropEvent = event;
          data.elementId = event.dataTransfer.getData('elementId');
          data.target = element;
          scope.$emit('FlowChartBox.ELEMENT_DROPPED',data);
        }
      });

      scope.$on('FlowChartBox.ELEMENT_DRAGSTART', (e, elementFrom) => {
        if (attrs.dropTargetTo === 'parent') {
          element[0].parentNode.classList.add('active-drag-target');
        }
      });

      scope.$on('FlowChartBox.ELEMENT_DRAGEND', (e, elementFrom) => {
        if (attrs.dropTargetTo === 'parent') {
          element[0].parentNode.classList.remove('active-drag-target');
        }
      });
    }
  };
}


exports.inject = function (module) {
  module.directive('dropTarget', DropTarget);
};
