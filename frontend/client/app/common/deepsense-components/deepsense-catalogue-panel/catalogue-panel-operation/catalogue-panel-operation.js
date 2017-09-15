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

import tpl from './catalogue-panel-operation.html';

function OperationItemView() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: tpl,
    scope: {
      'id': '@',
      'name': '@',
      'icon': '@',
      'isRunning': '='
    },
    link: (scope, elem) => {
      scope.highlight = false;
      scope.$on('ConnectionHinter.HIGHLIGHT_OPERATIONS', (_, data) => {
        scope.highlight = data[scope.id];
        scope.$digest();
      });
      scope.$on('ConnectionHinter.DISABLE_HIGHLIGHTINGS', () => {
        scope.highlight = false;
        scope.$digest();
      });
      scope.getIcons = function () {
        return scope.icon.split(' ');
      };

      elem.on('mousedown', () => {
        $('.popover').hide();
      });
      elem.on('wheel', () => {
        $('.popover').hide();
      });
      elem.on('dragstart', () => {
        $('.popover').hide();
      });
      elem.on('mouseover', () => {
        $('.popover').show();
      });
      elem.on('mouseup', () => {
        $('.popover').show();
      });
    }
  };
}
angular.module('deepsense-catalogue-panel').directive('operationItem', OperationItemView);
