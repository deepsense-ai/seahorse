/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericSelectorItem = require('./common-generic-selector-item.js');

function ColumnListSelectorItem(options) {
  this.type = ColumnListSelectorItem.getType();
  this.columns = [];

  options.item.values.forEach((val) => this.addColumn(val));
}

ColumnListSelectorItem.prototype = new GenericSelectorItem();
ColumnListSelectorItem.prototype.constructor = GenericSelectorItem;

ColumnListSelectorItem.prototype.addColumn = function addColumn(name) {
  this.columns.push({
    'name': name
  });
};

ColumnListSelectorItem.prototype.serialize = function serialize() {
  return {
    'type': this.type.id,
    'values': this.columns.map((column) => { return column.name; })
  };
};

ColumnListSelectorItem.getType = () => {
  return {
    'id': 'columnList',
    'verbose': 'Names list'
  };
};

ColumnListSelectorItem.prototype.containsField = function(field) {
  return this.columns.find((column) => field.name === column.name);
};

module.exports = ColumnListSelectorItem;
