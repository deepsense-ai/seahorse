/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 22.07.15.
 */

'use strict';

/* @ngInject */
function Droppable($log, DragAndDrop) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      if (!attrs.droppableType) {
        return false;
      }

      // TODO highlight
      var highlightTo;

      switch (attrs.droppableHighlight) {
        case 'parent':
          highlightTo = element.parent();
          break;
      }

      element.on('drop', drop);
      element.on('dragover', dragOver);

      function drop(event) {
        if (
          event.dataTransfer.getData('droppable') === 'true' &&
          event.dataTransfer.getData('draggableType') === attrs.droppableType
        ) {
          $log.info('Drop was on', element[0]);

          /* prevent bubbling */
          event.stopImmediatePropagation();

          DragAndDrop.drop(event, element);
        }
      }

      function dragOver(event) {
        /* prevent default behaviour */
        event.preventDefault();
        /* allow drop */
        return true;
      }
    }
  };
}

exports.inject = function (module) {
  module.directive('droppable', Droppable);
};
