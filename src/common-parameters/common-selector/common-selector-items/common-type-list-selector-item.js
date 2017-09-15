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
    'boolean': false,
    'categorical': false,
    'string': false,
    'timestamp': false
  };

  let types = options.item.values;
  for (let i = 0; i < types.length; ++i) {
    this.types[types[i]] = true;
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
