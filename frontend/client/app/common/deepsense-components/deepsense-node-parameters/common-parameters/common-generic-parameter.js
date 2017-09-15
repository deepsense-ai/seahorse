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

function GenericParameter() {}

GenericParameter.prototype.initValue = function(paramValue, paramSchema, replaceEmptyWithDefault) {
  this.defaultValue = paramSchema.default;
  if (!_.isUndefined(paramValue)) {
    this.value = paramValue;
  } else if (replaceEmptyWithDefault) {
    this.value = this.defaultValue;
  } else {
    this.value = null;
  }
};

GenericParameter.prototype.getValueOrDefault = function() {
  if (_.isUndefined(this.value) || _.isNull(this.value)) {
    return this.defaultValue;
  } else {
    return this.value;
  }
};

GenericParameter.prototype.serialize = () => {
  throw 'serialize method not defined';
};

GenericParameter.prototype.validate = () => true;

GenericParameter.prototype.refresh = (node) => { return undefined; };

module.exports = GenericParameter;
