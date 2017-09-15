/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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
