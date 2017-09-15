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
function Droppable($log, DragAndDrop) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      if (!attrs.droppableType) {
        return false;
      }

      // TODO highlight
      var highlightTo;

      if (attrs.droppableHighlight === 'parent') {
        highlightTo = element.parent();
      }

      element.on('drop', drop);
      element.on('dragover', (event) => {
        event.dataTransfer.effectAllowed = 'copy';
        event.dataTransfer.dropEffect = 'copy';
        event.preventDefault();
        return false;
      });
      element.on('dragenter', event => {
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
