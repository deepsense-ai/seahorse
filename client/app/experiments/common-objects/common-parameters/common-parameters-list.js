/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function ParametersList(options) {
  this.parameters = options.parameters;
}

ParametersList.prototype.serialize = function () {
  let serializedObject = {};

  for (let paramName in this.parameters) {
    let parameter = this.parameters[paramName];
    serializedObject[parameter.name] = parameter.serialize();
  }

  return serializedObject;
};

ParametersList.prototype.validate = function() {
  for (let parameter in this.parameters) {
    if (!parameter.validate()) {
      return false;
    }
  }

  return true;
};

module.exports = ParametersList;
