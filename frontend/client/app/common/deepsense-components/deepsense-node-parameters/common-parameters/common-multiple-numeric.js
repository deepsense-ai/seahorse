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

let GenericParameter = require('./common-generic-parameter.js');
let MultipleNumericGeneralValidator = require('./common-validators/common-multiple-numeric-general-validator.js');

function MultipleNumericParameter(options) {
    this.name = options.name;
    this.initValue(options.value, options.schema);
    this.schema = options.schema;
    this.validator = new MultipleNumericGeneralValidator(this.schema.validator);
}

MultipleNumericParameter.prototype = new GenericParameter();
MultipleNumericParameter.prototype.constructor = GenericParameter;

MultipleNumericParameter.prototype.rawValue = function() {
    return this.convertToRawValue(this.value);
};

MultipleNumericParameter.prototype.convertToRawValue = function(val) {
  if(_.isUndefined(val) || _.isNull(val) || _.isEmpty(val)) {
    return '';
  } else {
    let str = val.values[0].value.sequence.join(', ');
    return str;
  }
};

MultipleNumericParameter.prototype.parseRawValue = function(rawValue) {
    let values = this._parseAsArray(rawValue);
    return values && {
      values: [
        {
          type: 'seq',
          value: {
            sequence: values
          }
        }
      ]
    };
};

MultipleNumericParameter.prototype._parseAsArray = function(rawValue) {
    let commaSeparatedListOfNumbersRegex =
        new RegExp('^(\\s*-?\\d+\\.?\\d*\\s*,)*\\s*-?\\d+\\.?\\d*$');
    if (commaSeparatedListOfNumbersRegex.test(rawValue)) {
        return rawValue.replace(/\\s/g, '').split(',').map(parseFloat);
    } else {
        return null;
    }
};

MultipleNumericParameter.prototype.serialize = function () {
    return this.value;
};

MultipleNumericParameter.prototype.validate = function () {
    return this.validateValue(this.getValueOrDefault());
};

MultipleNumericParameter.prototype.validateRawValue = function (rawValue) {
    if (_.isNull(rawValue) || rawValue === '') {
        return this.validateValue(this.defaultValue);
    } else {
        return this.validateValue(this.parseRawValue(rawValue));
    }
};

MultipleNumericParameter.prototype.validateValue = function (value) {
    let values = (value === null || value === undefined) ? null : value.values[0].value.sequence;
    return this.validator.validate(values);
};

module.exports = MultipleNumericParameter;
