/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let StringRegexValidator = require('./common-string-regex-validator.js');
let NumericRangeValidator = require('./common-numeric-range-validator.js');
let MultipleNumericGeneralValidator = require('./common-multiple-numeric-general-validator.js');
let NoopValidator = require('./common-noop-validator.js');

let validatorConstructors = {
  'string-regex': StringRegexValidator,
  'numeric-range': NumericRangeValidator,
  'multipleNumeric-general': MultipleNumericGeneralValidator
};

let ValidatorFactory = {
  createValidator(paramType, validatorSchema) {
    if (validatorSchema && validatorSchema.type) {
      let Constructor = validatorConstructors[paramType + '-' + validatorSchema.type];
      return typeof Constructor === 'undefined' ?
        new NoopValidator() :
        new Constructor(validatorSchema);
    } else {
      return new NoopValidator();
    }
  }
};

module.exports = ValidatorFactory;
