/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeSingleChoiceType($compile) {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-single-choice-type.html',
    scope: true,
    replace: true,
    link: function (scope, element, attrs) {
      let internal = {};

      internal.initChoice = function initChoice () {
        scope.choice = null;
        for (let choiceName in scope.parameter.choices) {
          if (scope.parameter.choices[choiceName]) {
            scope.choice = choiceName;
            return;
          }
        }
      };

      internal.renderParametersList = function renderParametersList () {
        let template = `<attributes-list
          ng-repeat="(choiceName, parametersList) in ::parameter.possibleChoicesList"
          ng-if="parameter.choices[choiceName]"
          parameters-list="parametersList">
        </attributes-list>`;
        let $parametersListsContainer = angular.element(element[0].querySelector('.nested-attributes-view'));
        let $parametersListsEls = $compile(template)(scope);

        $parametersListsContainer.append($parametersListsEls);
      };

      internal.initChoice();
      internal.renderParametersList();

      scope.$watch('choice', function (newValue, oldValue) {
        if (newValue !== oldValue) {
          scope.parameter.choices[oldValue] = false;
          scope.parameter.choices[newValue] = true;
        }
      });
    }
  };
}


exports.inject = function (module) {
  module.directive('attributeSingleChoiceType', AttributeSingleChoiceType);
};
