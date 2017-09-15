/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function NoopValidator() {}

NoopValidator.prototype.validate = function(value) {
  return true;
};

module.exports = NoopValidator;
