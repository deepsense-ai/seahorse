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

import tpl from './attribute-multiple-choice-type.html';

/* @ngInject */
function AttributeMultipleChoiceType($compile) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    scope: true,
    replace: true,
    link: function (scope, element, attrs) {
      let internal = {};
      scope.name = scope.parameter.name.replace(/\s+/g, '_');

      internal.initChoices = function() {
        if (scope.parameter.isDefault) {
          scope.choices = {};
          for (let choiceName in scope.parameter.possibleChoicesList) {
            scope.choices[choiceName] = false;
          }
          let defaultValue = scope.parameter.schema.default;
          if (_.isObject(defaultValue)) {
            for (let choiceName in scope.parameter.possibleChoicesList) {
              scope.choices[choiceName] = choiceName in defaultValue;
            }
          } else if (defaultValue !== null) {
            scope.choices[defaultValue] = true;
          }
        } else {
          scope.choices = _.assign({}, scope.parameter.choices);
        }
      };

      internal.renderParametersList = function renderParametersList() {
        scope.$applyAsync(() => {
          for (let choiceName in scope.parameter.possibleChoicesList) {
            let $parametersListsContainer = angular.element(element[0].querySelector('#attr-list-' + scope.name + '-' + choiceName));
            let template = `<attributes-list
              parameters-list="parameter.possibleChoicesList[choiceName]"
              ng-if="parameter.choices[choiceName]"
            ></attributes-list>`;
            let $parametersListsEls = $compile(template)($parametersListsContainer.scope());

            $parametersListsContainer.append($parametersListsEls);
          }
        });
      };

      internal.initChoices();
      internal.renderParametersList();

      scope.$watch('choices', function (newVal, oldVal) {
        if (newVal !== oldVal) {
          scope.parameter.choices = newVal;
          scope.parameter.isDefault = false;
        }
      }, true);
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributeMultipleChoiceType', AttributeMultipleChoiceType);
