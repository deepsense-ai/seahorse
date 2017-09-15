'use strict';

let GenericParameter = require('./../common-generic-parameter.js');
let SelectorItemFactory = require('./common-selector-items/common-selector-item-factory.js');

function SelectorParameter(options, node) {
  this.factoryItem = SelectorItemFactory;
  this.name = options.name;
  let value = this.initValue(options.value, options.schema);
  this.items = this.initItems(value, options.schema);
  this.schema = options.schema;
  this.dataFrameSchema = options.dataFrameSchema;

  if (!this.schema.isSingle) {
    this.excluding = options.hasOwnProperty('excluding') ?
      options.excluding :
      false;
  }

  this.setDataFrameSchema(node);
}

SelectorParameter.prototype = new GenericParameter();
SelectorParameter.prototype.constructor = GenericParameter;

SelectorParameter.prototype.initItems = function(value, schema) {
  let isSingle = schema.isSingle;
  let result = [];

  if (isSingle) {
    let selectorItem = this.factoryItem.createItem(value);
    if (selectorItem) {
      result.push(selectorItem);
    }
  } else if (value) {
    for (let i = 0; i < value.length; ++i) {
      let selectorItem = this.factoryItem.createItem(value[i]);
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
    let result = {
      excluding: this.excluding,
      selections: []
    };

    for (let i = 0; i < this.items.length; ++i) {
      result.selections.push(this.items[i].serialize());
    }

    return result.selections.length === 0 ?
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

SelectorParameter.prototype.setDataFrameSchema = function(node) {
  this.dataFrameSchema = undefined;
  let dataFrameInputPort = this.schema.portIndex;
  let inputKnowledge = node.getIncomingKnowledge(dataFrameInputPort);
  if (inputKnowledge) {
    let inferredResultDetails = inputKnowledge.result;
    if (inferredResultDetails) {
      // We assume that if selector is declared, inferred result details have 'schema' field.
      this.dataFrameSchema = inferredResultDetails.schema;
    }
  }
};

SelectorParameter.prototype.refresh = function(node) {
  this.setDataFrameSchema(node);
};

module.exports = SelectorParameter;
