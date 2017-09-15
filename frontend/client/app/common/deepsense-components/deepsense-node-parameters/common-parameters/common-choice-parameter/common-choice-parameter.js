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

let GenericParameter = require('./../common-generic-parameter.js');

function ChoiceParameter() {}

ChoiceParameter.prototype = new GenericParameter();
ChoiceParameter.prototype.constructor = GenericParameter;

ChoiceParameter.prototype.init = function(options) {
  this.name = options.name;
  this.schema = options.schema;
  this.possibleChoicesList = _.assign({}, options.possibleChoicesList);

  this.choices = {};
  for (let choiceName in this.possibleChoicesList) {
    this.choices[choiceName] = false;
  }

  for (let i = 0; i < options.choices.length; ++i) {
    this.choices[options.choices[i]] = true;
  }
};

ChoiceParameter.prototype.serialize = function () {
  if (this.isDefault) {
    return null;
  }

  let serializedObject = {};

  for (let choiceName in this.choices) {
    if (this.choices[choiceName]) {
      serializedObject[choiceName] = this.possibleChoicesList[choiceName].serialize();
    }
  }

  return serializedObject;
};

ChoiceParameter.prototype.validate = function () {
  for (let choiceName in this.choices) {
    let paramIsValid = this.possibleChoicesList[choiceName].validate();
    if (!paramIsValid) {
      return false;
    }
  }

  return true;
};

ChoiceParameter.prototype.refresh = function(node) {
  _.forOwn(this.possibleChoicesList, (params, _) => params.refresh(node));
};

module.exports = ChoiceParameter;
