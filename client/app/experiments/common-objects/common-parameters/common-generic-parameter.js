/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function GenericParameter() {}

GenericParameter.prototype.initValue = function(paramValue, paramSchema) {
  if (typeof paramValue === 'undefined') {
    return paramSchema.default;
  } else {
    return paramValue;
  }
};

GenericParameter.prototype.serialize = () => {
  throw 'serialize method not defined';
};

GenericParameter.prototype.validate = () => true;

module.exports = GenericParameter;
