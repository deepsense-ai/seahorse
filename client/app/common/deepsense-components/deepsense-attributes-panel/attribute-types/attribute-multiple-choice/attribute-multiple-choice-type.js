/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
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
            let $parametersListsContainer = angular.element(element[0].querySelector('#attr-list-' + scope.parameter.name + '-' + choiceName));
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
