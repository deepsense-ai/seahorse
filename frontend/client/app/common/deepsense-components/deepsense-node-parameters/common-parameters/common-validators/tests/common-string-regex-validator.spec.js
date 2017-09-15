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
