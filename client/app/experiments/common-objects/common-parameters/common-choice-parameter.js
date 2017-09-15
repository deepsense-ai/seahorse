/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function ChoiceParameter() {}

ChoiceParameter.prototype = new GenericParameter();
ChoiceParameter.prototype.constructor = GenericParameter;

ChoiceParameter.prototype.init = function(options) {
  this.name = options.name;
  this.schema = options.schema;
  this.possibleChoicesList = options.possibleChoicesList;

  this.choices = {};
  for (let choiceName in this.possibleChoicesList) {
    this.choices[choiceName] = false;
  }
  for (let choiceName of options.choices) {
    this.choices[choiceName] = true;
  }
};

ChoiceParameter.prototype.serialize = function () {
  let serializedObject = {};

  for (let choiceName in this.choices) {
    if (this.choices[choiceName]) {
      serializedObject[choiceName] = this.possibleChoicesList[choiceName].serialize();
    }
  }

  return serializedObject;
};

ChoiceParameter.prototype.validate = function () {
  for (let choiceName in this.choices) {
    let paramIsValid = this.possibleChoicesList[choiceName].validate();
    if (!paramIsValid) {
      return false;
    }
  }

  return true;
};

module.exports = ChoiceParameter;
