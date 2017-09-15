/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');
let ValidatorFactory = require('./common-validators/common-validator-factory.js');

function StringParameter(options) {
  this.name = options.name;
  this.value = this.initValue(options.value, options.schema);
  this.schema = options.schema;
  this.validator = ValidatorFactory.createValidator(this.schema.type, this.schema.validator);
}

StringParameter.prototype = new GenericParameter();
StringParameter.prototype.constructor = GenericParameter;

StringParameter.prototype.serialize = function () {
  return this.value;
};

StringParameter.prototype.validate = function () {
  return this.validator.validate(this.value);
};

module.exports = StringParameter;
