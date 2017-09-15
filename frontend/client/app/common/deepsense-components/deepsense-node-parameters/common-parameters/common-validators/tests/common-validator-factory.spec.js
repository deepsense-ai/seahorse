/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

describe('Validator factory', () => {
  var ValidatorFactory = require('./../common-validator-factory.js');
  let StringRegexValidator = require('./../common-string-regex-validator.js');
  let NumericRangeValidator = require('./../common-numeric-range-validator.js');
  let NoopValidator = require('./../common-noop-validator.js');

  it('is defined', () => {
    expect(ValidatorFactory).toBeDefined();
    expect(ValidatorFactory).toEqual(jasmine.any(Object));
  });

  it('has createValidator method', () => {
    expect(ValidatorFactory.createValidator).toBeDefined();
    expect(ValidatorFactory.createValidator).toEqual(jasmine.any(Function));
  });

  it('creates proper constructors', () => {
    expect(ValidatorFactory.createValidator('string', { 'type': 'regex'})).toEqual(jasmine.any(StringRegexValidator));
    expect(ValidatorFactory.createValidator('string', { 'type': 'other'})).toEqual(jasmine.any(NoopValidator));
    expect(ValidatorFactory.createValidator('numeric', { 'type': 'range'})).toEqual(jasmine.any(NumericRangeValidator));
    expect(ValidatorFactory.createValidator('numeric', { 'type': 'other'})).toEqual(jasmine.any(NoopValidator));
  });
});
