/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function IndexListSelectorItem(options) {
  this.type = IndexListSelectorItem.getType();
  this.firstNum = options.item.values[0] || 0;
  this.secondNum = options.item.values[1] || 0;
}

IndexListSelectorItem.prototype = new GenericSelectorItem();
IndexListSelectorItem.prototype.constructor = GenericSelectorItem;

IndexListSelectorItem.prototype.serialize = function serialize() {
  return {
    'type': 'indexList',
    'values': [
      this.firstNum,
      this.secondNum
    ]
  };
};

IndexListSelectorItem.prototype.validate = function () {
  return this.firstNum <= this.secondNum;
};

IndexListSelectorItem.getType = () => { return {
  'id': 'indexList',
  'verbose': 'Indexes list'
};};

module.exports = IndexListSelectorItem;
