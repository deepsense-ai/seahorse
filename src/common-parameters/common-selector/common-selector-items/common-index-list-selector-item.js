/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function IndexListSelectorItem(options) {
  this.type = IndexListSelectorItem.getType();
  this.firstNum = options.item.values[0];
  this.secondNum = options.item.values[1];
}

IndexListSelectorItem.prototype = new GenericSelectorItem();
IndexListSelectorItem.prototype.constructor = GenericSelectorItem;

IndexListSelectorItem.prototype.serialize = function serialize() {
  let isDefined = (v) => !_.isUndefined(v) && !_.isNull(v);

  return {
    'type': 'indexRange',
    'values': isDefined(this.firstNum) && isDefined(this.secondNum) ?
      [
        this.firstNum,
        this.secondNum
      ] :
      []
  };
};

IndexListSelectorItem.prototype.validate = function () {
  return this.firstNum <= this.secondNum;
};

IndexListSelectorItem.getType = () => { return {
  'id': 'indexRange',
  'verbose': 'Indexes list'
};};

module.exports = IndexListSelectorItem;
