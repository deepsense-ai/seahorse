/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Numeric range validator', () => {
  var NumericRangeValidator = require('./../common-numeric-range-validator.js');

  it('constructor is defined', () => {
    expect(NumericRangeValidator).toBeDefined();
    expect(NumericRangeValidator).toEqual(jasmine.any(Function));
  });

  it('has validate method', () => {
    let validator = new NumericRangeValidator();

    expect(validator.validate).toBeDefined();
    expect(validator.validate).toEqual(jasmine.any(Function));
  });

  it('does not validate the value which does not belong to the interior of the range', () => {
    let validator = new NumericRangeValidator({
      'type': 'range',
      'configuration': {
        'begin': 41,
        'end': 43
      }
    });

    expect(validator.validate(40)).toBe(false);
    expect(validator.validate(44)).toBe(false);
  });

  describe('validates the value which belongs to the interior of the range', () => {
    it('', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': 41,
          'end': 43
        }
      });

      expect(validator.validate(42)).toBe(true);
    });

    it('and additionally is a multiple of the step', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': 40,
          'end': 46,
          'step': 1.5,
          'beginIncluded': true,
          'endIncluded': false
        }
      });

      expect(validator.validate(40)).toBe(true);
      expect(validator.validate(41.5)).toBe(true);
      expect(validator.validate(43)).toBe(true);
      expect(validator.validate(44.5)).toBe(true);
      expect(validator.validate(46)).toBe(false);

      expect(validator.validate(42)).toBe(false);
      expect(validator.validate(44)).toBe(false);
    });
  });

  it('does not validate the value which belongs to the boundary of the range', () => {
    let validator = new NumericRangeValidator({
      'type': 'range',
      'configuration': {
        'begin': 41,
        'end': 43,
        'beginIncluded': false,
        'endIncluded': false
      }
    });

    expect(validator.validate(41)).toBe(false);
    expect(validator.validate(43)).toBe(false);
  });

  it('validates the value which belongs to the boundary of the range', () => {
    let validator = new NumericRangeValidator({
      'type': 'range',
      'configuration': {
        'begin': 41,
        'end': 43,
        'beginIncluded': true,
        'endIncluded': true
      }
    });

    expect(validator.validate(41)).toBe(true);
    expect(validator.validate(43)).toBe(true);
  });

  it('does not validate the value which belongs to the interior of the range, but is not a multiple of the step', () => {
    let validator = new NumericRangeValidator({
      'type': 'range',
      'configuration': {
        'begin': 40.0010105,
        'end': 44,
        'step': 2.22504
      }
    });

    expect(validator.validate(41)).toBe(false);
    expect(validator.validate(42.2260504)).toBe(false);
    expect(validator.validate(42.2260505)).toBe(true);
    expect(validator.validate(42.2260506)).toBe(false);
  });

});
