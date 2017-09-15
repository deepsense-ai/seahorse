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

let GenericParameter = require('./common-generic-parameter.js');

function MultiplierParameter(options) {
  this.name = options.name;
  this.schema = options.schema;
  this.parametersLists = options.parametersLists;
  this.emptyItem = options.emptyItem;
}

MultiplierParameter.prototype = new GenericParameter();
MultiplierParameter.prototype.constructor = GenericParameter;

MultiplierParameter.prototype.serialize = function () {
  return _.map(this.parametersLists, param => param.serialize());
};

MultiplierParameter.prototype.validate = function () {
  _.forEach(this.parametersLists, param => {
    let isValid = param.validate();
    if (!isValid) {
      return false;
    }
  });

  return true;
};

MultiplierParameter.prototype.refresh = function (node) {
  _.forEach(this.parametersLists, param => {
    param.refresh(node);
  });
};

module.exports = MultiplierParameter;
