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

require('./attributes-serialized-view/attributes-serialized-view.js');
require('./calculated-selected-columns/calculated-selected-columns.js');
require('./selector-items/selector-items.js');

import selectorTypeTpl from './attribute-selector-type.html';
import selectorTypeModalTpl from './attribute-selector-type-modal.html';

/* @ngInject */
function AttributeSelectorType($timeout, $uibModal, $rootScope) {
  return {
    restrict: 'E',
    templateUrl: selectorTypeTpl,
    replace: true,
    scope: true,
    link: function(scope, element) {
      let selectorIsSingle = () => scope.parameter.schema.isSingle;
      let selectorItemFactory = scope.parameter.factoryItem;
      let types = selectorIsSingle() ?
          selectorItemFactory.getAllItemsTypes().singleSelectorItems :
          selectorItemFactory.getAllItemsTypes().multipleSelectorItems;

      let clearEmptyParameters = function clearEmptyParameters() {
        _.forEachRight(scope.parameter.items, (parameter, index) => {
          if (scope.isEmptyParameter(parameter)) {
            scope.removeItem(index);
          }
        });
      };

      _.assign(scope, {
        itemTypes: types,
        modal: null,
        openSelector() {
          let oldParameter = JSON.stringify(scope.parameter.serialize());

          this.modal = $uibModal.open({
            templateUrl: selectorTypeModalTpl,
            size: selectorIsSingle() ? 'md' : 'lg',
            scope: scope,
            windowClass: 'selection-modal'
          });

          this.modal.result
            .finally(() => {
              clearEmptyParameters();

              let currParameter = JSON.stringify(scope.parameter.serialize());
              if (currParameter !== oldParameter) {
                oldParameter = currParameter;
              }
            });
        },
        isEmptyParameter(parameter) {
          switch (parameter.type.id) {
            case 'indexRange':
              return _.isUndefined(parameter.firstNum) || _.isUndefined(parameter.secondNum);
            case 'columnList':
              return parameter.columns.length === 0;
            case 'typeList':
              return !_.some(_.values(parameter.types));
            // no default
          }
        },
        isItemIdInList(id) {
          return _.find(scope.parameter.items, (lists) => lists.type.id === id);
        },
        getItemsThisType(id) {
          return _.filter(scope.parameter.items, (lists) => lists.type.id === id);
        },
        getItemsThisTypeOrDefault(id) {
          if ((this.selectorIsSingle() || !scope.parameter.excluding) && _.isEmpty(scope.parameter.items)) {
            return _.filter(scope.parameter.defaultItems, (lists) => lists.type.id === id);
          } else {
            return this.getItemsThisType(id);
          }
        },
        getCurrentItemIndex(item) {
          return scope.parameter.items.indexOf(item);
        },
        addItem(id) {
          let selectorItem = selectorItemFactory.createItem({
            'type': id,
            'values': []
          });

          scope.parameter.items.push(selectorItem);
          return selectorItem;
        },
        removeItem(itemIndex) {
          this.parameter.items.splice(itemIndex, 1);
        },
        switchItem(item) {
          scope.parameter.items.splice(0, 1);
          this.addItem(item.id);
        },
        selectorIsSingle: selectorIsSingle,
        showItemsChoices() {
          let isSingle = this.selectorIsSingle();
          return !isSingle || (isSingle && this.parameter.items.length !== 1);
        }
      });

      // should init with initial correct value
      scope.$applyAsync(() => {
        $timeout(() => {
          jQuery('[data-action="excluding"]', element).click();
        });
      });
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributeSelectorType', AttributeSelectorType);
