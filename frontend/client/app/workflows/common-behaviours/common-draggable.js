/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

/* @ngInject */
function Draggable($rootScope, $log, DragAndDrop) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      let doNotListen = false;
      let isTargetCorrect;

      if (!attrs.draggableType) {
        return false;
      }

      element.on('mousedown', mousedown);
      element.on('dragstart', dragstart);
      element.on('dragend', dragend);

      function mousedown(event) {
        /* check cascade */
        doNotListen = !isTargetCorrect(event.target.closest('[draggable]'));
      }

      function dragstart(event) {
        if(!event.target.draggable) {
          // Workaround for SHR-32. Problem might be somewhere else or even in Chrome itself.
          // TODO Fix this at problems root instead of workarounding here
          $log.warn('Dragstart called on element with draggable=false.');
          return true;
        }

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

          DragAndDrop.drag(event, element);
        }
      }

      function dragend(event) {
        if (isTargetCorrect(event.target)) {

          $rootScope.$broadcast('Drag.END', event, element);

          event.preventDefault();

          return false;
        }
      }

      /* privates */
      isTargetCorrect = function isTargetCorrect(element, target) {
        return element === target;
      }.bind(undefined, element[0]);
    }
  };
}

exports.inject = function(module) {
  module.directive('draggable', Draggable);
};
