/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeMultiplierType($compile, $rootScope, DeepsenseNodeParameters) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-multiplier/attribute-multiplier-type.html',
    replace: true,
    link: function(scope, element) {
      let internal = {};

      internal.renderParametersLists = function renderParametersLists () {
        let template = `
        <div class="ibox multiplier-item"
             ng-repeat="parametersList in parameter.parametersLists">
          <div class="ibox-title">
            <span class="ibox-title--multiplier">Group #{{ $index + 1 }}</span>
            <a class="close-link" aria-label="Close" ng-click="removeItem($index)">
              <i class="fa fa-times"></i>
            </a>
          </div>
          <div class="ibox-content">
            <attributes-list parameters-list="parametersList"></attributes-list>
          </div>
        </div>`;
        let $parametersListsContainer = angular.element(element[0].querySelector('.nested-attributes-view'));
        let $parametersListsEls = $compile(template)(scope);

        $parametersListsContainer.append($parametersListsEls);
      };

      internal.renderParametersLists();

      _.assign(scope, {
        addItem() {
          this.parameter.parametersLists.push(
            DeepsenseNodeParameters.factory.createParametersList({}, this.parameter.schema.values)
          );
        },
        removeItem(itemIndex) {
          if (window.confirm('Are you sure to remove the multiplier item?')) {
            this.parameter.parametersLists.splice(itemIndex, 1);

            $rootScope.$applyAsync(() => {
              $rootScope.$broadcast('AttributesPanel.UPDATED');
            });
          }
        }
      });

      if (scope.parameter.parametersLists.length === 0) {
        scope.addItem();
      }
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeMultiplierType', AttributeMultiplierType);
