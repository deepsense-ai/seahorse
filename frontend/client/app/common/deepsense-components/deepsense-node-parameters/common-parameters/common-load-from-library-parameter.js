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

let GenericParameter = require('./common-generic-parameter.js');

function LoadFromLibraryParameter(options) {
  this.name = options.name;
  this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

LoadFromLibraryParameter.prototype = new GenericParameter();
LoadFromLibraryParameter.prototype.constructor = GenericParameter;

LoadFromLibraryParameter.prototype.serialize = function () {
  return this.value;
};

module.exports = LoadFromLibraryParameter;
