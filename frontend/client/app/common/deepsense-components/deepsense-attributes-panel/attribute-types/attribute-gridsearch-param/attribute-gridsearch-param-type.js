/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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
