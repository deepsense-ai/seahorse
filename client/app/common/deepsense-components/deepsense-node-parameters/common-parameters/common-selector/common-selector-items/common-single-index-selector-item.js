/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function SingleIndexSelectorItem(options) {
  this.type = SingleIndexSelectorItem.getType();
  this.firstNum = options.item.value;
}

SingleIndexSelectorItem.prototype = new GenericSelectorItem();
SingleIndexSelectorItem.prototype.constructor = GenericSelectorItem;

SingleIndexSelectorItem.prototype.serialize = function serialize() {
  return this.firstNum >= 0 && !_.isNull(this.firstNum) ? {
    type: 'index',
    value: this.firstNum
  } : null;
};

SingleIndexSelectorItem.getType = () => {
  return {
    'id': 'index',
    'verbose': 'Select by index'
  };
};

SingleIndexSelectorItem.prototype.containsField = function(field, index) {
  return this.firstNum === index;
};

module.exports = SingleIndexSelectorItem;
