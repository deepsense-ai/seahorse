/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function AttributeChoiceType($compile) {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-choice-type.html',
    scope: true,
    link: function (scope, element, attrs) {
      function clearOutOldAttributeView(node) {
        let childrenNodes = node.children();
        for (let i = 0; i < childrenNodes.length; ++i) {
          angular.element(childrenNodes[i]).children().scope().$destroy();
        }

        node.empty();
      }

      function updateNestedAttributeView(scope) {
        let paramName = scope.paramName,
          el = angular.element(element[0].querySelector('#attr-view-children-' + paramName)),
          template,
          newAttributesView;

        clearOutOldAttributeView(el);

        template = `<attributes-list
          param-schemas="paramSchemas[paramName].values[choices[paramName]]"
          param-values="paramValues[paramName][choices[paramName]]"
        ></attributes-list>`;

        newAttributesView = $compile(template)(scope);

        el.append(newAttributesView);
      }

      function initChoice() {
        let choices = Object.keys(scope.paramValues[scope.paramName]);
        if (choices.length) {
          scope.choices[scope.paramName] = choices[0];

          scope.$applyAsync(() => {
            updateNestedAttributeView(scope);
          });
        }
      }

      scope.choices = {};

      initChoice();

      scope.$watch('choices[paramName]', function (newValue, oldValue) {
        if (newValue !== oldValue) {
          updateNestedAttributeView(scope);
        }
      });
    }
  };
}


exports.inject = function (module) {
  module.directive('attributeChoiceType', AttributeChoiceType);
};
