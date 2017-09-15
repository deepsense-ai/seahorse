'use strict';

/* @ngInject */
function Droppable($log, DragAndDrop) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
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
      element.on('dragover', (event) => {
        event.dataTransfer.effectAllowed = 'copy';
        event.preventDefault();
        return false;
      });
      element.on('dragenter', event => {
        event.dataTransfer.dropEffect = 'copy';
        event.preventDefault();
        return false;
      });

      function drop(event) {
        if (
          event.dataTransfer.getData('droppable') === 'true' &&
          event.dataTransfer.getData('draggableType') === attrs.droppableType
        ) {
          $log.debug('Drop was on', element[0]);

          /* prevent bubbling */
          event.stopImmediatePropagation();

          DragAndDrop.drop(event, element);

          event.preventDefault();

          return false;
        }
      }
    }
  };
}

exports.inject = function(module) {
  module.directive('droppable', Droppable);
};
