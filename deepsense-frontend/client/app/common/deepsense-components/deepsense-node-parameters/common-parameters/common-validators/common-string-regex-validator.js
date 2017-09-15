/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericValidator = require('./common-generic-validator.js');

function StringRangeValidator(validatorSchema) {
  this.schema = validatorSchema;
}

StringRangeValidator.prototype = new GenericValidator();
StringRangeValidator.prototype.constructor = GenericValidator;

StringRangeValidator.prototype.validate = function(value) {
  if (typeof value !== 'string') {
    return false;
  } else {
    let re = new RegExp(this.schema.configuration.regex);
    return re.test(value);
  }
};

module.exports = StringRangeValidator;
