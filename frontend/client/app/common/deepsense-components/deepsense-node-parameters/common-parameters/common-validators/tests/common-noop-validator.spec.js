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
