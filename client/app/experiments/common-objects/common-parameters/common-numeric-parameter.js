/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function NumericParameter(options) {
  this.name = options.name;
  this.value = this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

NumericParameter.prototype = new GenericParameter();
NumericParameter.prototype.constructor = GenericParameter;

NumericParameter.prototype.serialize = function () {
  return this.value;
};

NumericParameter.prototype.validate = function () {
  // TODO: access this._schema.validator and validate this._value
};

module.exports = NumericParameter;
