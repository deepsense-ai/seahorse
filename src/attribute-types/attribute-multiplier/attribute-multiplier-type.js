/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeMultiplierType($compile, DeepsenseNodeParameters) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-multiplier/attribute-multiplier-type.html',
    replace: true,
    link: function(scope, element) {
      let internal = {};

      internal.renderParametersLists = function renderParametersLists () {
        let template = `
        <div class="panel panel-default multiplier-item"
          ng-repeat="parametersList in parameter.parametersLists"
        >
          <div class="panel-heading">
            Multiplier #{{ $index + 1 }}
            <button type="button" class="close pull-right" data-dismiss="alert" aria-label="Close" ng-click="removeItem($index)">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="panel-body">
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
          }
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeMultiplierType', AttributeMultiplierType);
