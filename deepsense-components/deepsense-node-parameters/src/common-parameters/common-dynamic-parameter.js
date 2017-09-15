'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function DynamicParameter({name, value, schema}, node, paramsFactory) {
  this.name = name;
  this.initValue(value, schema);
  this.schema = schema;
  this.paramsFactory = paramsFactory;

  this.setInternalParams(node);
}

DynamicParameter.prototype = new GenericParameter();
DynamicParameter.prototype.constructor = GenericParameter;

DynamicParameter.prototype.serialize = function () {
  return this.internalParams ? this.internalParams.serialize() : {};
};

DynamicParameter.prototype.setInternalParams = function (node) {
  let inputPort = this.schema.inputPort;
  let incomingKnowledge = node.getIncomingKnowledge(inputPort);
  let inferredResultDetails = incomingKnowledge && incomingKnowledge.result;
  this.internalParamsAvailable = inferredResultDetails ? true : false;

  if (this.internalParamsAvailable) {
    // We assume that if dynamic params is declared, inferred result details have 'params' field.
    let inferredParams = inferredResultDetails.params;

    // Here we pass inferred parameter values as defaults to dynamic parameters.
    let dynamicParamsSchema = inferredParams.schema.slice(0);
    _.forEach(inferredParams.values, function (value, key) {
      let schemaEntry = _.find(dynamicParamsSchema, function (item) {
        return item.name == key;
      });
      schemaEntry.default = value;
    });

    // Here we overwrite inferred values with values specified by user.
    // If at any point we wish to present information which values were overwritten,
    // here is the place to get this information.
    let dynamicParamsValues = _.assign({}, this.value, this.serialize());

    this.internalParams = this.paramsFactory.createParametersList(
        dynamicParamsValues,
        dynamicParamsSchema,
        node
    );
  }
};

DynamicParameter.prototype.refresh = function (node) {
  this.setInternalParams(node);
  if (this.internalParams) {
    this.internalParams.refresh(node);
  }
};

module.exports = DynamicParameter;
