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

import tpl from './attribute-single-choice-type.html';

/* @ngInject */
function AttributeSingleChoiceType($compile) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    scope: true,
    replace: true,
    link: function (scope, element) {
      let internal = {};

      internal.initChoice = function initChoice() {
        scope.choice = null;
        for (let choiceName in scope.parameter.choices) {
          if (scope.parameter.choices[choiceName]) {
            scope.choice = choiceName;
            return;
          }
        }
        if (scope.choice === null) {
          let defaultValue = scope.parameter.schema.default;
          if (_.isObject(defaultValue)) {
            scope.choice = Object.keys(defaultValue)[0];
          } else {
            scope.choice = defaultValue;
          }
        }
      };

      internal.renderParametersList = function renderParametersList() {
        let template = `<attributes-list
          ng-repeat="(choiceName, parametersList) in ::parameter.possibleChoicesList"
          ng-if="choice === choiceName"
          parameters-list="parametersList">
        </attributes-list>`;
        let $parametersListsContainer = angular.element(element[0].querySelector('.nested-attributes-view'));
        let $parametersListsEls = $compile(template)(scope);
        $parametersListsContainer.append($parametersListsEls);
      };

      internal.initChoice();
      internal.renderParametersList();

      scope.isValueSelectedByUser = function () {
        let itemSetToTrue = _.find(scope.parameter.choices, function(value) { return value; });
        return !_.isUndefined(itemSetToTrue);
      };

      scope.$watch('choice', function (newValue, oldValue) {
        if (newValue !== oldValue) {
          scope.parameter.choices[oldValue] = false;
          scope.parameter.choices[newValue] = true;
        }
      });

      // When the user edits an inner value of a default choice parameter, the whole parameter becomes user-selected.
      scope.$watch('parameter.possibleChoicesList', function (newValue, oldValue) {
        if (newValue !== oldValue) {
          scope.parameter.choices[scope.choice] = true;
        }
      }, true);
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributeSingleChoiceType', AttributeSingleChoiceType);
