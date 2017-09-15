/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let ChoiceParameter = require('./common-choice-parameter.js');

function SingleChoiceParameter(options) {
  options.choices = this.initChoices(options);

  this.init(options);
}

SingleChoiceParameter.prototype = new ChoiceParameter();
SingleChoiceParameter.prototype.constructor = ChoiceParameter;

SingleChoiceParameter.prototype.initChoices = function (options) {
  if (options.value) {
    return Object.keys(options.value);
  } else {
    let defaultValue = options.schema.default;
    return defaultValue ?
      [ defaultValue ] :
      [];
  }
};

SingleChoiceParameter.prototype.serialize = function () {
  let serializedObject = ChoiceParameter.prototype.serialize.call(this);

  if (Object.keys(serializedObject).length > 1) {
    throw `too many choices in the SingleChoiceParameter object: ${ this.name }`;
  }

  return serializedObject;
};

SingleChoiceParameter.prototype.validate = function () {
  let choicesNumber = 0;
  for (let choiceName in this.choices) {
    if (this.choices[choiceName]) {
      ++choicesNumber;
    }
  }

  if (choicesNumber > 1) {
    return false;
  } else {
    return ChoiceParameter.prototype.validate.call(this);
  }
};

module.exports = SingleChoiceParameter;
