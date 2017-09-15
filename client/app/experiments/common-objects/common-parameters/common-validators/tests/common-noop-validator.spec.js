/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Noop validator', () => {
  var NoopValidator = require('./../common-noop-validator.js');

  it('constructor is defined', () => {
    expect(NoopValidator).toBeDefined();
    expect(NoopValidator).toEqual(jasmine.any(Function));
  });

  let validator = new NoopValidator();

  it('has validate method', () => {
    expect(validator.validate).toBeDefined();
    expect(validator.validate).toEqual(jasmine.any(Function));
  });

  it('validates any value positively', () => {
    expect(validator.validate('test')).toBe(true);
    expect(validator.validate(42)).toBe(true);
    expect(validator.validate({})).toBe(true);
    expect(validator.validate([])).toBe(true);
    expect(validator.validate(false)).toBe(true);
    expect(validator.validate(() => {})).toBe(true);
  });
});
