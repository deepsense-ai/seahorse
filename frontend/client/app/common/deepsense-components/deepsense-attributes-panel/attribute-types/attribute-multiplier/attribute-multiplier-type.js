/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


'use strict';

import tpl from './attribute-multiplier-type.html';

/* @ngInject */
function AttributeMultiplierType($compile, $rootScope, DeepsenseNodeParameters) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    link: function(scope, element) {
      let internal = {};

      internal.renderParametersLists = function renderParametersLists() {
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
          this.parameter.parametersLists.push(angular.copy(scope.parameter.emptyItem));
        },
        removeItem(itemIndex) {
          if (window.confirm('Are you sure to remove the multiplier item?')) {
            this.parameter.parametersLists.splice(itemIndex, 1);
          }
        }
      });

      if (scope.parameter.parametersLists.length === 0) {
        scope.addItem();
      }
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributeMultiplierType', AttributeMultiplierType);
