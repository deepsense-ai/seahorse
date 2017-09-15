/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericValidator = require('./common-generic-validator.js');

function NoopValidator() {}

NoopValidator.prototype = new GenericValidator();
NoopValidator.prototype.constructor = GenericValidator;

NoopValidator.prototype.validate = function() {
  return true;
};

module.exports = NoopValidator;
