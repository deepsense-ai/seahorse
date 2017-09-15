/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericValidator = require('./common-generic-validator.js');

function NumericRangeValidator(validatorSchema) {
  this.schema = validatorSchema;
}

NumericRangeValidator.prototype = new GenericValidator();
NumericRangeValidator.prototype.constructor = GenericValidator;

NumericRangeValidator.prototype.validate = function(value) {
  if (typeof value !== 'number') {
    return false;
  } else {
    let rangeBegin = this.schema.configuration.begin;
    let rangeEnd = this.schema.configuration.end;
    let step = this.schema.configuration.step;

    if (value < rangeBegin || value > rangeEnd) {
      return false;
    } else if (value === rangeBegin) {
      return this.schema.configuration.beginIncluded;
    } else if (value === rangeEnd) {
      return this.schema.configuration.endIncluded;
    } else {
      if (!step) {
        return true;
      } else {
        /*
         * (value - rangeBegin) needs to be a multiple of step
         */

        /* returns the length of the decimal part of the number */
        let getLengthOfDecimalPart = (num) => {
          let numStr = num.toString();
          let commaPos = numStr.indexOf('.');
          return commaPos === -1 ? -1 : numStr.length - commaPos - 1;
        };

        /* returns num * 10^power */
        let getMultipliedNumber = (num, power) => {
          let lengthOfDecimalPart = getLengthOfDecimalPart(num);
          let multipliedNumberStr = num.toString().replace('.', '').concat(_.repeat('0', power - lengthOfDecimalPart));
          return parseInt(multipliedNumberStr, 10);
        };

        let valueCommaPos = getLengthOfDecimalPart(value);
        let rangeBeginCommaPos = getLengthOfDecimalPart(rangeBegin);
        let stepCommaPos = getLengthOfDecimalPart(step);
        let maxCommaPos = Math.max.call(null, valueCommaPos, rangeBeginCommaPos, stepCommaPos);

        if (maxCommaPos === -1) {
          return (value - rangeBegin) % step === 0;
        } else {
          let multipliedValue = getMultipliedNumber(value, maxCommaPos);
          let multipliedRangeBegin = getMultipliedNumber(rangeBegin, maxCommaPos);
          let multipliedStep = getMultipliedNumber(step, maxCommaPos);

          return (multipliedValue - multipliedRangeBegin) % multipliedStep === 0;
        }
      }
    }
  }
};

module.exports = NumericRangeValidator;
