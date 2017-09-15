/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function SingleIndexSelectorItem(options) {
  this.type = SingleIndexSelectorItem.getType();
  this.firstNum = options.item.value || 0;
}

SingleIndexSelectorItem.prototype = new GenericSelectorItem();
SingleIndexSelectorItem.prototype.constructor = GenericSelectorItem;

SingleIndexSelectorItem.prototype.serialize = function serialize() {
  return {
    type: 'index',
    value: this.firstNum
  };
};

SingleIndexSelectorItem.getType = () => { return {
  'id': 'index',
  'verbose': 'Single index'
};};

module.exports = SingleIndexSelectorItem;
