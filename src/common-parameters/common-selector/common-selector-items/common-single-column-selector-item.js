/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function SingleColumnSelectorItem(options) {
  this.type = SingleColumnSelectorItem.getType();
  this.addColumn(options.item.value);
}

SingleColumnSelectorItem.prototype = new GenericSelectorItem();
SingleColumnSelectorItem.prototype.constructor = GenericSelectorItem;

SingleColumnSelectorItem.prototype.addColumn = function addColumn(name) {
  this.column = {
    'name': name
  };
};

SingleColumnSelectorItem.prototype.serialize = function serialize() {
  return this.column.name ? {
    'type': this.type.id,
    'value': this.column.name
  } : null;
};

SingleColumnSelectorItem.getType = () => { return {
  'id': 'column',
  'verbose': 'Select by name'
};};

module.exports = SingleColumnSelectorItem;
