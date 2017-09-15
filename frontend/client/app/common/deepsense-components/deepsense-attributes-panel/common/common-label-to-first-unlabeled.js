/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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
function LabelToFirstUnlabeled($timeout) {
  return {
    restrict: 'A',
    link: (scope, element) => {
      scope.$applyAsync(() => {
        var nextSibling = element[0].nextElementSibling;
        var uniqueId = String(Math.random()).substring(2);
        var nextCheckbox;

        if (
          nextSibling &&
          (
            nextCheckbox = nextSibling.querySelector('input') ||
            nextSibling.querySelector('textarea') ||
            nextSibling.querySelector('button')
          ) &&
          nextCheckbox.parentNode.tagName !== 'LABEL'
        ) {
          nextCheckbox.id = uniqueId;
          $('label', element).attr('for', uniqueId);
        }
      });
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('labelToFirstUnlabeled', LabelToFirstUnlabeled);
