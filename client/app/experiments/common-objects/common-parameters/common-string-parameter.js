/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function StringParameter(options) {
  this.name = options.name;
  this.value = this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

StringParameter.prototype = new GenericParameter();
StringParameter.prototype.constructor = GenericParameter;

StringParameter.prototype.serialize = function () {
  return this.value;
};

StringParameter.prototype.validate = function () {
  // TODO: access this._schema.validator and validate this._value
};

module.exports = StringParameter;
