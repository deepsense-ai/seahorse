/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    let isUndefined = (v) => _.isNull(v) || _.isUndefined(v);

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
        return isUndefined(step) ?
          v < rB :
          isMultipleOfStep(v, rB);
      }
    };
    let belongsToLeftBoundedRightInfiniteInterval = (lB, v) => {
      if (v === lB) {
        return beginIncluded;
      } else {
        return isUndefined(step) ?
          lB < v :
          isMultipleOfStep(v, lB);
      }
    };
    let belongsToBoundedInterval = (lB, v, rB) => {
      if (v === lB) {
        return beginIncluded;
      } else if (v === rB) {
        return endIncluded;
      } else if (isUndefined(step)) {
        return (lB < v) && (v < rB);
      } else {
        return (lB < v) && (v < rB) && isMultipleOfStep(v, lB);
      }
    };

    if (isUndefined(rangeBegin) && isUndefined(rangeEnd)) {
      return true;
    } else if (isUndefined(rangeBegin)) {
      return belongsToLeftInfiniteRightBoundedInterval(value, rangeEnd);
    } else if (isUndefined(rangeEnd)) {
      return belongsToLeftBoundedRightInfiniteInterval(rangeBegin, value);
    } else {
      return belongsToBoundedInterval(rangeBegin, value, rangeEnd);
    }
  }
};

module.exports = NumericRangeValidator;
