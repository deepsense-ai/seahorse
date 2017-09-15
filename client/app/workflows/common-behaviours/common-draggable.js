/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 22.07.15.
 */

'use strict';

/* @ngInject */
function Draggable($rootScope, $log, DragAndDrop) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      let doNotListen = false;
      let isTargetCorrect;

      if (!attrs.draggableType) {
        return false;
      }

      element.on('mousedown', mousedown);
      element.on('dragstart', dragstart);
      element.on('dragend', dragend);

      function mousedown (event) {
        /* check cascade */
        doNotListen = !isTargetCorrect(event.target.closest('[draggable]'));
      }

      function dragstart (event) {
        $log.debug('Drag started on', element[0]);

        /* filter to cascaded draggable elements */
        if (doNotListen) {
          event.preventDefault();
          return false;
        }

        if (isTargetCorrect(event.target)) {
          $log.debug('Drag target is %s', 'correct');

          event.dataTransfer.setData('elementId', element[0].id);
          event.dataTransfer.setData('droppable', true);
          event.dataTransfer.setData('draggableType', attrs.draggableType);
          event.dataTransfer.setData('draggableExactType', attrs.draggableExactType);

          /* prevent bubbling */
          event.stopImmediatePropagation();

          DragAndDrop.drag(event, element);
        }
      }

      function dragend (event) {
        if (isTargetCorrect(event.target)) {
          event.dataTransfer.setData('draggableType', attrs.draggableType);

          $rootScope.$broadcast('Drag.END', event, element);

          event.preventDefault();

          return false;
        }
      }

      /* privates */
      isTargetCorrect = function isTargetCorrect (element, target) {
        return element === target;
      }.bind(undefined, element[0]);
    }
  };
}

exports.inject = function (module) {
  module.directive('draggable', Draggable);
};
