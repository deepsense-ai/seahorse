/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./../common-generic-parameter.js');
let SelectorItemFactory = require('./common-selector-items/common-selector-item-factory.js');

function SelectorParameter(options) {
  this.name = options.name;
  this.items = this.initItems(options.value, options.schema);
  this.schema = options.schema;
}

SelectorParameter.prototype = new GenericParameter();
SelectorParameter.prototype.constructor = GenericParameter;

SelectorParameter.prototype.initItems = function(value, schema) {
  let isSingle = schema.isSingle;
  let result = [];

  if (isSingle) {
    let selectorItem = SelectorItemFactory.createItem(value);
    if (selectorItem) {
      result.push(selectorItem);
    }
  } else if (value) {
    for (let i = 0; i < value.length; ++i) {
      let selectorItem = SelectorItemFactory.createItem(value[i]);
      if (selectorItem) {
        result.push(selectorItem);
      }
    }
  }

  return result;
};

SelectorParameter.prototype.serialize = function () {
  if (this.schema.isSingle) {
    return this.items.length === 0 ?
      null :
      this.items[0].serialize();
  } else {
    let result = [];
    for (let i = 0; i < this.items.length; ++i) {
      result.push(this.items[i].serialize());
    }

    return result.length === 0 ?
      null :
      result;
  }
};

SelectorParameter.prototype.validate = function() {
  if (this.schema.isSingle && this.items.length > 1) {
    return false;
  } else {
    for (let i = 0; i < this.items.length; ++i) {
      let isValid = this.items[i].validate();
      if (!isValid) {
        return false;
      }
    }

    return true;
  }
};

module.exports = SelectorParameter;
