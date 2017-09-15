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
        var data = {};
        data.dropEvent = event;
        data.classId = event.dataTransfer.getData('classId');
        data.target = element;
        $rootScope.$emit('FlowChartBox.ELEMENT_DROPPED',data);
      });
    }
  };
}


exports.inject = function (module) {
  module.directive('dropTarget', DropTarget);
};
