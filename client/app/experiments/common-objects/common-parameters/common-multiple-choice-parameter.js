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
  return typeof options.value === 'undefined' ?
    options.schema.default.slice() :
    Object.keys(options.value);
};

module.exports = MultipleChoiceParameter;
