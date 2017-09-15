/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

function SingleChoiceParameter(options) {
  options.choices = this.initChoices(options);

  this.init(options);
}

SingleChoiceParameter.prototype = new ChoiceParameter();
SingleChoiceParameter.prototype.constructor = ChoiceParameter;

SingleChoiceParameter.prototype.initChoices = function (options) {
  if (options.value) {
    return Object.keys(options.value);
  } else {
    return [];
  }
};

SingleChoiceParameter.prototype.serialize = function () {
  let serializedObject = ChoiceParameter.prototype.serialize.call(this);
  let choicesNum = Object.keys(serializedObject).length;

  if (choicesNum > 1) {
    throw `too many choices in the SingleChoiceParameter object: ${this.name}`;
  }

  return choicesNum === 0 ?
    null :
    serializedObject;
};

SingleChoiceParameter.prototype.validate = function () {
  let choicesNumber = 0;
  for (let choiceName in this.choices) {
    if (this.choices[choiceName]) {
      ++choicesNumber;
    }
  }

  if (choicesNumber > 1) {
    return false;
  } else {
    return ChoiceParameter.prototype.validate.call(this);
  }
};

module.exports = SingleChoiceParameter;
