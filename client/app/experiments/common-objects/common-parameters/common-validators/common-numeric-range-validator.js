/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function NumericRangeValidator(validatorSchema) {
  this.schema = validatorSchema;
}

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
        let isInt = (n) => n % 1 === 0;
        while (!isInt(value) || !isInt(rangeBegin) || !isInt(rangeEnd)) {
          value *= 10;
          rangeBegin *= 10;
          rangeEnd *= 10;
        }

        let r = (value - rangeBegin) / step;

        return isInt(r);
      }
    }
  }
};

module.exports = NumericRangeValidator;
