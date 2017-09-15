/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function GenericValidator() {}

GenericValidator.prototype.validate = () => {
  throw 'validate method has not been defined';
};

module.exports = GenericValidator;
