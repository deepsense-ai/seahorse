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

import tpl from './attributes-list.html';

/* @ngInject */
function AttributesList(AttributesPanelService) {

  return {
    require: '^deepsenseOperationAttributes',
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      parametersList: '=',
      isRootLevelParameter: '@' // undefined => not root parameter
    },
    link: function (scope, element, _, attributesPanelCtrl) {
      scope.noParamValues = () => (Object.keys(scope.parametersList.parameters).length === 0);

      // Exposing dynamic params results in having dynamic param of dynamic param in outside workflow.
      // This causes stack overflow. Quick fix is to prevent from exposing dynamic params
      scope.isDynamic = (parameter) => {
        return parameter.schema.type === 'dynamic';
      };

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
