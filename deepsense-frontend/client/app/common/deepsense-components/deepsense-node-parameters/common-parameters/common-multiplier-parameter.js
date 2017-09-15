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
  this.emptyItem = options.emptyItem;
}

MultiplierParameter.prototype = new GenericParameter();
MultiplierParameter.prototype.constructor = GenericParameter;

MultiplierParameter.prototype.serialize = function () {
  return _.map(this.parametersLists, param => param.serialize());
};

MultiplierParameter.prototype.validate = function () {
  _.forEach(this.parametersLists, param => {
    let isValid = param.validate();
    if (!isValid) {
      return false;
    }
  });

  return true;
};

MultiplierParameter.prototype.refresh = function (node) {
  _.forEach(this.parametersLists, param => {
    param.refresh(node);
  });
};

module.exports = MultiplierParameter;
