'use strict';

import tpl from './attribute-gridsearch-param-type.html';

/* @ngInject */
function AttributeGridSearchParamType($compile, DynamicParamTypeService) {

  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: true,
    link: (scope, element) => {
      let internal = {};
      internal.renderParametersList = function renderParametersList() {
        scope.$applyAsync(() => {
          let $parametersListContainer = angular.element(element[0].querySelector('.nested-attributes-view'));
          let containerScope = $parametersListContainer.scope();
          DynamicParamTypeService.addBufferedInternalParamsWatches(scope, containerScope);
          let template = `
            <attributes-list
              parameters-list="bufferedInternalParams"
              ng-if="parameter.internalParamsAvailable"
            ></attributes-list>
            <p ng-if="!parameter.internalParamsAvailable">
              Parameters can not be inferred in current state
            </p>`;
          let $renderedParametersList = $compile(template)(containerScope);
          $parametersListContainer.append($renderedParametersList);
        });
      };

      internal.renderParametersList();
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributeGridsearchParamType', AttributeGridSearchParamType);
