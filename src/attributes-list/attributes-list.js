/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributesList(AttributesPanelService) {
  return {
    restrict: 'E',
    templateUrl: 'attributes-list/attributes-list.html',
    replace: true,
    scope: {
      'parametersList': '='
    },
    link: function (scope, element) {
      scope.noParamValues = () => (Object.keys(scope.parametersList.parameters).length === 0);

      scope.$watch('parametersList.parameters', () => {
        scope.$applyAsync(() => {
          // get rendered content
          AttributesPanelService.disableElements(element);
        });
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributesList', AttributesList);
