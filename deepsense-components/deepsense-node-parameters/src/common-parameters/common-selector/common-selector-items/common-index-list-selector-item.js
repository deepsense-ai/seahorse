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
  let values = [];

  if (isDefined(this.firstNum)) {
    values.push(this.firstNum);
    if (isDefined(this.secondNum)) {
      values.push(this.secondNum);
    }
  }

  return {
    'type': 'indexRange',
    'values': values
  };
};

IndexListSelectorItem.prototype.validate = function () {
  return this.firstNum <= this.secondNum;
};

IndexListSelectorItem.getType = () => { return {
  'id': 'indexRange',
  'verbose': 'Index range'
};};

IndexListSelectorItem.prototype.containsField = function(field, index, fieldsCount) {
  return this.secondNum < fieldsCount && index >= this.firstNum && index <= this.secondNum;
};

module.exports = IndexListSelectorItem;
