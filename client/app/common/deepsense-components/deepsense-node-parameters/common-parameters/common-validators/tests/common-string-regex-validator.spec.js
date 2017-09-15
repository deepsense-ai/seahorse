/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('String regex validator', () => {
  var StringRegexValidator = require('./../common-string-regex-validator.js');

  it('constructor is defined', () => {
    expect(StringRegexValidator).toBeDefined();
    expect(StringRegexValidator).toEqual(jasmine.any(Function));
  });

  it('has validate method', () => {
    let validator = new StringRegexValidator();

    expect(validator.validate).toBeDefined();
    expect(validator.validate).toEqual(jasmine.any(Function));
  });

  it('validates the value which matches against the regular expression', () => {
    let validator = new StringRegexValidator({
      'type': 'range',
      'configuration': {
        'regex': '^\\d{2}[+-]\\d{3}[*/]\\d{4}'
      }
    });

    expect(validator.validate('10+100*1000')).toBe(true);
    expect(validator.validate('55-555/5555')).toBe(true);
    expect(validator.validate('00+000/0000 ')).toBe(true);
    expect(validator.validate('99-999*9999-')).toBe(true);
    expect(validator.validate('99-999*9999abcd')).toBe(true);
  });

  it('does not validate the value which does not match against the regular expression', () => {
    let validator = new StringRegexValidator({
      'type': 'range',
      'configuration': {
        'regex': '^ABCD$'
      }
    });

    expect(validator.validate('')).toBe(false);
    expect(validator.validate('ABCDE')).toBe(false);
    expect(validator.validate(' ABCD')).toBe(false);
    expect(validator.validate('ABCD ')).toBe(false);
    expect(validator.validate('BCD')).toBe(false);
  });
});
