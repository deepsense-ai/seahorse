/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeMultipleChoiceType($compile) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-multiple-choice/attribute-multiple-choice-type.html',
    scope: true,
    replace: true,
    link: function (scope, element, attrs) {
      let internal = {};

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

      internal.renderParametersList();
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeMultipleChoiceType', AttributeMultipleChoiceType);
