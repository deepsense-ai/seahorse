'use strict';

let GenericParameter = require('./common-generic-parameter.js');
let MultipleNumericGeneralValidator = require('./common-validators/common-multiple-numeric-general-validator.js');

function MultipleNumericParameter(options) {
    this.name = options.name;
    this.value = this.initValue(options.value, options.schema);
    this.schema = options.schema;
    this.validator = new MultipleNumericGeneralValidator(this.schema.validator);
}

MultipleNumericParameter.prototype = new GenericParameter();
MultipleNumericParameter.prototype.constructor = GenericParameter;

MultipleNumericParameter.prototype.rawValue = function() {
    if(_.isUndefined(this.value) || _.isNull(this.value) || _.isEmpty(this.value)) {
        return '';
    } else {
        return this.value.values[0].value.sequence.join(', ');
    }
};

MultipleNumericParameter.prototype.parseRawValue = function(rawValue) {
    let values = this._parseAsArray(rawValue);
    return values && {
      values: [
        {
          type: "seq",
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
    return this.validateValue(this.value);
};

MultipleNumericParameter.prototype.validateValue = function (value) {
    let values = value == null ? null : value.values[0].value.sequence;
    return this.validator.validate(values);
};

module.exports = MultipleNumericParameter;
