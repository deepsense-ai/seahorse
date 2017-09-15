/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function GenericParameter() {}

GenericParameter.prototype.initValue = function(paramValue, paramSchema, replaceEmptyWithDefault) {
  this.defaultValue = paramSchema.default;
  if (!_.isUndefined(paramValue)) {
    this.value = paramValue;
  } else if (replaceEmptyWithDefault) {
    this.value = this.defaultValue;
  } else {
    this.value = null;
  }
};

GenericParameter.prototype.getValueOrDefault = function() {
  if (_.isUndefined(this.value) || _.isNull(this.value)) {
    return this.defaultValue;
  } else {
    return this.value;
  }
};

GenericParameter.prototype.serialize = () => {
  throw 'serialize method not defined';
};

GenericParameter.prototype.validate = () => true;

GenericParameter.prototype.refresh = (node) => { return undefined; };

module.exports = GenericParameter;
