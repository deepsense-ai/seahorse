/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function GenericParameter() {}

GenericParameter.prototype.initValue = function(paramValue, paramSchema) {
  if (_.isUndefined(paramValue)) {
    return paramSchema.default;
  } else {
    return paramValue;
  }
};

GenericParameter.prototype.serialize = () => {
  throw 'serialize method not defined';
};

GenericParameter.prototype.validate = () => true;

GenericParameter.prototype.refresh = (node) => { return undefined; };

module.exports = GenericParameter;
