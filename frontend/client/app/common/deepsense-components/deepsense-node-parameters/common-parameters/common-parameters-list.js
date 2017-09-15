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

function ParametersList(options) {
  this.parameters = options.parameters;
}

ParametersList.prototype.serialize = function () {
  return _.reduce(this.parameters, (acc, parameter) => {
    let serializedParameter = parameter.serialize();
    acc[parameter.name] = serializedParameter;
    return acc;
  }, {});
};

ParametersList.prototype.validate = function() {
  return _.every(_.map(this.parameters, (parameter) => parameter.validate()));
};

ParametersList.prototype.refresh = function(node) {
  _.forEach(this.parameters, param => {
    param.refresh(node);
  });
};

module.exports = ParametersList;
