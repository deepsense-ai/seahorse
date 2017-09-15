/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function MultiplierParameter(options) {
  this.name = options.name;
  this.schema = options.schema;
  this.parametersLists = options.parametersLists;
}

MultiplierParameter.prototype = new GenericParameter();
MultiplierParameter.prototype.constructor = GenericParameter;

MultiplierParameter.prototype.serialize = function () {
  let serializedData = [];
  for (let i = 0; i < this.parametersLists.length; ++i) {
    serializedData.push(this.parametersLists[i].serialize());
  }
  return serializedData;
};

MultiplierParameter.prototype.validate = function () {
  for (let i = 0; i < this.parametersLists.length; ++i) {
    let isValid = this.parametersLists[i].validate();
    if (!isValid) {
      return false;
    }
  }

  return true;
};

module.exports = MultiplierParameter;
