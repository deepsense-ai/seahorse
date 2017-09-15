/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

var internal = {};

/* @ngInject */
function DropTarget($rootScope) {
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
        var box = element[0].getBoundingClientRect();
        console.log(box);
        $rootScope.$emit('FlowChartBox.ELEMENT_DROPPED', {
          classId: event.dataTransfer.getData('classId'),
          clientX: event.dataTransfer.getData('clientX'),
          clientY: event.dataTransfer.getData('clientY')
        });
      });
    }
  };
}


exports.inject = function (module) {
  module.directive('dropTarget', DropTarget);
};
