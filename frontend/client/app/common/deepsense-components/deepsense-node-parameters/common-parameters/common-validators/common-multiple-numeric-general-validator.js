'use strict';

let GenericValidator = require('./common-generic-validator.js');
let NumericRangeValidator = require('./common-numeric-range-validator.js');

function MultipleNumericGeneralValidator(singleValueValidatorSchema) {
    if (!_.isUndefined(singleValueValidatorSchema)) {
        this.singleValueValidator = new NumericRangeValidator(singleValueValidatorSchema);
    }
}

MultipleNumericGeneralValidator.prototype = new GenericValidator();
MultipleNumericGeneralValidator.prototype.constructor = GenericValidator;

MultipleNumericGeneralValidator.prototype.validate = function(values) {
    if (values === null) {
        return false;
    } else if (!_.isUndefined(this.singleValueValidator)) {
        return this._validateValues(values);
    } else {
        return true;
    }
};

MultipleNumericGeneralValidator.prototype._validateValues = function(values) {
    let validationResult = true;
    _.forEach(values, (value) => {
        if (!this.singleValueValidator.validate(value)) {
            validationResult = false;
            return false;
        }
    });
    return validationResult;
};

module.exports = MultipleNumericGeneralValidator;
