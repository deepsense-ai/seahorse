/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let SelectorItemFactory = require('./../../../../common-objects/common-parameters/common-selector/common-selector-items/common-selector-item-factory.js');

function AttributeSelectorType() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-column-selector/attribute-selector-type.html',
    replace: true,
    scope: true,
    link: function(scope) {
      let selectorIsSingle = () => scope.parameter.schema.isSingle;
      let types = selectorIsSingle() ?
        SelectorItemFactory.getAllItemsTypes().singleSelectorItems :
        SelectorItemFactory.getAllItemsTypes().multipleSelectorItems;

      _.assign(scope, {
        createItemType: types[0],
        itemTypes: types,
        addItem() {
          let selectorItem = SelectorItemFactory.createItem({
            'type': this.createItemType.id,
            'values': []
          });

          scope.parameter.items.push(selectorItem);
        },
        removeItem(itemIndex) {
          if (window.confirm('Are you sure to remove the selector item?')) {
            scope.parameter.items.splice(itemIndex, 1);
          }
        },
        selectorIsSingle: selectorIsSingle,
        showItemsChoices() {
          let isSingle = this.selectorIsSingle();
          return !isSingle || (isSingle && scope.parameter.items.length !== 1);
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('attributeSelectorType', AttributeSelectorType);
};
