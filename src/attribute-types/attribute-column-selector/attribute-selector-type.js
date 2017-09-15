/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributeSelectorType($timeout) {
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
        createItemType: types[0],
        itemTypes: types,
        addItem() {
          let selectorItem = selectorItemFactory.createItem({
            'type': this.createItemType.id,
            'values': []
          });

          scope.parameter.items.push(selectorItem);
        },
        removeItem(itemIndex) {
          if (window.confirm('Are you sure to remove the selector item?')) {
            this.parameter.items.splice(itemIndex, 1);
          }
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