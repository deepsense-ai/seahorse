/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function TypeListSelectorItem(options) {
  this.type = TypeListSelectorItem.getType();

  this.types = {
    'numeric': false,
    'ordinal': false,
    'boolean': false,
    'categorical': false,
    'string': false,
    'timestamp': false,
    'time interval': false
  };

  for (let type of options.item.values) {
    this.types[type] = true;
  }
}

TypeListSelectorItem.prototype = new GenericSelectorItem();
TypeListSelectorItem.prototype.constructor = GenericSelectorItem;

TypeListSelectorItem.prototype.serialize = function () {
  return {
    'type': 'typeList',
    'values': Object.keys(this.types).filter((type) => this.types[type])
  };
};

TypeListSelectorItem.getType = () => { return {
  'id': 'typeList',
  'verbose': 'Types list'
};};

module.exports = TypeListSelectorItem;
