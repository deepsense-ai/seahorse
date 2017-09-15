'use strict';

import tpl from './attribute-single-choice-type.html';

/*@ngInject*/
function AttributeSingleChoiceType($compile) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    scope: true,
    replace: true,
    link: function (scope, element) {
      let internal = {};

      internal.initChoice = function initChoice () {
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

      internal.renderParametersList = function renderParametersList () {
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

angular.module('deepsense.attributes-panel').
    directive('attributeSingleChoiceType', AttributeSingleChoiceType);
