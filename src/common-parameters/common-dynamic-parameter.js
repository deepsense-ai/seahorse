'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function DynamicParameter({name, value, schema, internalParams}) {
  this.name = name;
  this.value = this.initValue(value, schema);
  this.schema = schema;
  this.internalParams = internalParams;
}

DynamicParameter.prototype = new GenericParameter();
DynamicParameter.prototype.constructor = GenericParameter;

DynamicParameter.prototype.serialize = function () {
  return this.internalParams && this.internalParams.serialize();
};

module.exports = DynamicParameter;
