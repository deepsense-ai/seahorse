'use strict';

/*@ngInject*/
function AttributeDynamicParamType($compile) {

  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-dynamic-param/attribute-dynamic-param-type.html',
    replace: true,
    scope: true,
    link: (scope, element) => {
      let internal = {};

      internal.renderParametersList = function renderParametersList() {
        scope.$applyAsync(() => {
          let $parametersListContainer = angular.element(element[0].querySelector('.nested-attributes-view'));
          let template = `
            <attributes-list
              parameters-list="parameter.internalParams"
              ng-if="parameter.internalParams"
            ></attributes-list>
            <p ng-if="!parameter.internalParams">
              Parameters can not be inferred in current state
            </p>`;
          let $renderedParametersList = $compile(template)($parametersListContainer.scope());
          $parametersListContainer.append($renderedParametersList);
        });
      };

      internal.renderParametersList();
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeDynamicParamType', AttributeDynamicParamType);
