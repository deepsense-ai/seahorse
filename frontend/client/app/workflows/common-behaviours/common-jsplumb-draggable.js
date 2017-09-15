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

import jsPlumb from 'jsplumb';

/* @ngInject */
function DraggableDirective($rootScope, GraphNode) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      var reInitThisNodePosition = function reInitThisNodePosition() {
        if (scope.node) {
          scope.node.x = parseInt(element.css('left'), 10);
          scope.node.y = parseInt(element.css('top'), 10);
        }
      };

      jsPlumb.draggable(element, {
        containment: 'parent',
        stop: () => {
          reInitThisNodePosition();
          $rootScope.$broadcast(GraphNode.MOVE);
        }
      });

      scope.$on('MultipleSelection.STOP_DRAG', reInitThisNodePosition);
    }
  };
}

exports.inject = function(module) {
  module.directive('jsplumbDraggable', DraggableDirective);
};
