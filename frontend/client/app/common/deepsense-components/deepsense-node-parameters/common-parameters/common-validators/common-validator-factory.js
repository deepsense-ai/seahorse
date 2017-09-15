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
