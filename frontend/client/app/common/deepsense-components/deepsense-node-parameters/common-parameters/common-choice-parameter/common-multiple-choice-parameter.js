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

let ChoiceParameter = require('./common-choice-parameter.js');

function MultipleChoiceParameter(options) {
  options.choices = this.initChoices(options);

  this.init(options);
}

MultipleChoiceParameter.prototype = new ChoiceParameter();
MultipleChoiceParameter.prototype.constructor = ChoiceParameter;

MultipleChoiceParameter.prototype.initChoices = function (options) {
  if (options.value) {
    this.isDefault = false;
    return Object.keys(options.value);
  } else {
    this.isDefault = true;
    return [];
  }
};

module.exports = MultipleChoiceParameter;
