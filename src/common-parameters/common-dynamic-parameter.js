'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function DynamicParameter({name, value, schema}, node, paramsFactory) {
  this.name = name;
  this.value = this.initValue(value, schema);
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

  if (inferredResultDetails) {
    // We assume that if dynamic params is declared, inferred result details have 'params' field.
    let inferredParams = inferredResultDetails.params;

    // Here we overwrite inferred values with values specified by user.
    // If at any point we wish to present information which values were overwritten,
    // here is the place to get this information.
    _.assign(inferredParams.values, this.value, this.serialize());

    this.internalParams = this.paramsFactory.createParametersList(
        inferredParams.values,
        inferredParams.schema,
        node
    );
  } else {
    this.internalParams = undefined;
  }
};

DynamicParameter.prototype.refresh = function (node) {
  this.setInternalParams(node);
  if (this.internalParams) {
    this.internalParams.refresh(node);
  }
};

module.exports = DynamicParameter;
