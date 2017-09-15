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
