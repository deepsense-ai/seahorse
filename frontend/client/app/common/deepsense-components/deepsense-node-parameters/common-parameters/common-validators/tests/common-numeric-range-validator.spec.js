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

  describe('does not validate the value which does not belong to the interior of the range', () => {
    it('. The interval is bounded on both sides.', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': 41,
          'end': 43,
          'step': null
        }
      });

      expect(validator.validate(40)).toBe(false);
      expect(validator.validate(44)).toBe(false);
    });

    it('. The interval is bounded on the left side and unbounded on the right side', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': 41,
          'end': null,
          'step': null
        }
      });

      expect(validator.validate(-100)).toBe(false);
      expect(validator.validate(0)).toBe(false);
      expect(validator.validate(40)).toBe(false);
    });

    it('. The interval is unbounded on the left side and bounded on the right side', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': null,
          'end': 100,
          'step': null
        }
      });

      expect(validator.validate(101)).toBe(false);
      expect(validator.validate(110)).toBe(false);
      expect(validator.validate(1000000)).toBe(false);
    });
  });

  describe('validates the value which belongs to the interior of the range', () => {
    it('. The internal is unbounded.', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {}
      });

      expect(validator.validate(-100)).toBe(true);
      expect(validator.validate(0)).toBe(true);
      expect(validator.validate(200)).toBe(true);
    });

    it('. The interval is bounded on both sides.', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': 41,
          'end': 43,
          'step': null
        }
      });

      expect(validator.validate(41.5)).toBe(true);
      expect(validator.validate(42)).toBe(true);
      expect(validator.validate(42.63929)).toBe(true);
    });

    it('. The interval is bounded on the left side and unbounded on the right side', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': 41,
          'end': null,
          'step': null
        }
      });

      expect(validator.validate(42)).toBe(true);
      expect(validator.validate(50)).toBe(true);
      expect(validator.validate(40000)).toBe(true);
    });

    it('. The interval is unbounded on the left side and bounded on the right side', () => {
      let validator = new NumericRangeValidator({
        'type': 'range',
        'configuration': {
          'begin': null,
          'end': 100,
          'step': null
        }
      });

      expect(validator.validate(-101)).toBe(true);
      expect(validator.validate(-1)).toBe(true);
      expect(validator.validate(99)).toBe(true);
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

      expect(validator.validate(38.5)).toBe(false);
      expect(validator.validate(42)).toBe(false);
      expect(validator.validate(44)).toBe(false);
      expect(validator.validate(47.5)).toBe(false);
    });
  });

  it('does not validate the value which belongs to the boundary of the range', () => {
    let validator = new NumericRangeValidator({
      'type': 'range',
      'configuration': {
        'begin': 41,
        'end': 43,
        'beginIncluded': false,
        'endIncluded': false,
        'step': null
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
        'endIncluded': true,
        'step': null
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
