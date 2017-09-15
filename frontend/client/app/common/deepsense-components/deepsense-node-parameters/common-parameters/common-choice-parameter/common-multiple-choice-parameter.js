/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let ChoiceParameter = require('./common-choice-parameter.js');

function MultipleChoiceParameter(options) {
  options.choices = this.initChoices(options);

  this.init(options);
}

MultipleChoiceParameter.prototype = new ChoiceParameter();
MultipleChoiceParameter.prototype.constructor = ChoiceParameter;

MultipleChoiceParameter.prototype.initChoices = function (options) {
  if (options.value) {
    this.isDefault = false;
    return Object.keys(options.value);
  } else {
    this.isDefault = true;
    return [];
  }
};

module.exports = MultipleChoiceParameter;
