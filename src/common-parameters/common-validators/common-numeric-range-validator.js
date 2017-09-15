/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
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

    let beginIncluded = this.schema.configuration.beginIncluded;
    let endIncluded = this.schema.configuration.endIncluded;

    /* checks whether |v - boundary| is a multiple of step */
    let isMultipleOfStep = (v, boundary) => {
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

      let valueDecLength = getLengthOfDecimalPart(v);
      let boundaryDecLength = getLengthOfDecimalPart(boundary);
      let stepDecLength = getLengthOfDecimalPart(step);
      let maxDecLength = Math.max.call(null, valueDecLength, boundaryDecLength, stepDecLength);

      if (maxDecLength === -1) {
        return Math.abs(v - boundary) % step === 0;
      } else {
        let multipliedValue = getMultipliedNumber(v, maxDecLength);
        let multipliedBoundary = getMultipliedNumber(boundary, maxDecLength);
        let multipliedStep = getMultipliedNumber(step, maxDecLength);

        return Math.abs(multipliedValue - multipliedBoundary) % multipliedStep === 0;
      }
    };
    let belongsToLeftInfiniteRightBoundedInterval = (v, rB) => {
      if (v === rB) {
        return endIncluded;
      } else {
        return _.isNull(step) ?
          v < rB :
          isMultipleOfStep(v, rB);
      }
    };
    let belongsToLeftBoundedRightInfiniteInterval = (lB, v) => {
      if (v === lB) {
        return beginIncluded;
      } else {
        return _.isNull(step) ?
          lB < v :
          isMultipleOfStep(v, lB);
      }
    };
    let belongsToBoundedInterval = (lB, v, rB) => {
      if (v === lB) {
        return beginIncluded;
      } else if (v === rB) {
        return endIncluded;
      } else {
        return _.isNull(step) ?
          (lB < v) && (v < rB) :
          isMultipleOfStep(v, lB);
      }
    };

    if (_.isNull(rangeBegin) && _.isNull(rangeEnd)) {
      return true;
    } else if (_.isNull(rangeBegin)) {
      return belongsToLeftInfiniteRightBoundedInterval(value, rangeEnd);
    } else if (_.isNull(rangeEnd)) {
      return belongsToLeftBoundedRightInfiniteInterval(rangeBegin, value);
    } else {
      return belongsToBoundedInterval(rangeBegin, value, rangeEnd);
    }
  }
};

module.exports = NumericRangeValidator;
