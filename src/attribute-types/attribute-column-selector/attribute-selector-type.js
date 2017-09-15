/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeSelectorType($timeout, $modal) {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/attribute-selector-type.html',
    replace: true,
    scope: true,
    link: function(scope, element) {
      let selectorIsSingle = () => scope.parameter.schema.isSingle;
      let selectorItemFactory = scope.parameter.factoryItem;
      let types = selectorIsSingle() ?
          selectorItemFactory.getAllItemsTypes().singleSelectorItems :
          selectorItemFactory.getAllItemsTypes().multipleSelectorItems;

      _.assign(scope, {
        itemTypes: types,
        modal: null,

        openSelector() {
          this.modal = $modal.open({
            templateUrl: 'attribute-types/attribute-column-selector/attribute-selector-type-modal.html',
            size: 'lg',
            scope: scope,
            windowClass: 'selection-modal'
          });
        },
        checkUnique(id) {
          return !!(id === 'columnList' || id === 'typesList');
        },
        isItemIdInList(id) {
          return _.find(scope.parameter.items, (lists) => lists.type.id === id);
        },
        getItemsThisType(id) {
          return _.filter(scope.parameter.items, (lists) => lists.type.id === id);
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
        },
        removeItem(itemIndex) {
          this.parameter.items.splice(itemIndex, 1);
        },
        switchItem (item) {
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

angular.module('deepsense.attributes-panel').
    directive('attributeSelectorType', AttributeSelectorType);