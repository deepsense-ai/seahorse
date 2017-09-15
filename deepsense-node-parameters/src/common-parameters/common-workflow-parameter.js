'use strict';

let GenericParameter = require('./common-generic-parameter.js');
let ValidatorFactory = require('./common-validators/common-validator-factory.js');

function WorkflowParameter(options) {
  this.name = options.name;
  this.initValue(options.value, options.schema, true);
  this.schema = options.schema;
  this.validator = ValidatorFactory.createValidator(this.schema.type, this.schema.validator);
}

WorkflowParameter.prototype = new GenericParameter();
WorkflowParameter.prototype.constructor = GenericParameter;

WorkflowParameter.prototype.serialize = function () {
  return this.value;
};

WorkflowParameter.prototype.validate = function () {
  return this.validator.validate(this.value);
};

module.exports = WorkflowParameter;
