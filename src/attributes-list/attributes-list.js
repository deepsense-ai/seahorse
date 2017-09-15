'use strict';

/*@ngInject*/
function AttributesList(AttributesPanelService) {

  return {
    require: '^deepsenseOperationAttributes',
    restrict: 'E',
    templateUrl: 'attributes-list/attributes-list.html',
    replace: true,
    scope: {
      parametersList: '=',
      isRootLevelParameter: '@' // undefined => not root parameter
    },
    link: function (scope, element, _, attributesPanelCtrl) {
      scope.noParamValues = () => (Object.keys(scope.parametersList.parameters).length === 0);

      scope.isInnerWorkflow = () => {
        return attributesPanelCtrl.isInnerWorkflow();
      };

      scope.setVisibility = (parameterName, visibility) => {
        attributesPanelCtrl.setVisibility(parameterName, visibility);
      };

      scope.getVisibility = (parameterName) => {
        return attributesPanelCtrl.getVisibility(parameterName);
      };

      // TODO Add explanation why is that needed.
      scope.$watch('parametersList.parameters', () => {
        scope.$applyAsync(() => {
          // get rendered content
          AttributesPanelService.disableElements(element);
        });
      });
    }
  };
}

angular.module('deepsense.attributes-panel').directive('attributesList', AttributesList);
