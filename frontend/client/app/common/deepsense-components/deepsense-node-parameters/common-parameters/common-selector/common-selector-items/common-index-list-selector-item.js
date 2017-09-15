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

let GenericSelectorItem = require('./common-generic-selector-item.js');

function IndexListSelectorItem(options) {
  this.type = IndexListSelectorItem.getType();
  this.firstNum = options.item.values[0];
  this.secondNum = options.item.values[1];
}

IndexListSelectorItem.prototype = new GenericSelectorItem();
IndexListSelectorItem.prototype.constructor = GenericSelectorItem;

IndexListSelectorItem.prototype.serialize = function serialize() {
  let isDefined = (v) => !_.isUndefined(v) && !_.isNull(v);
  let values = [];

  if (isDefined(this.firstNum)) {
    values.push(this.firstNum);
    if (isDefined(this.secondNum)) {
      values.push(this.secondNum);
    }
  }

  return {
    'type': 'indexRange',
    'values': values
  };
};

IndexListSelectorItem.prototype.validate = function () {
  return this.firstNum <= this.secondNum;
};

IndexListSelectorItem.getType = () => {
  return {
    'id': 'indexRange',
    'verbose': 'Index range'
  };
};

IndexListSelectorItem.prototype.containsField = function(field, index, fieldsCount) {
  return this.secondNum < fieldsCount && index >= this.firstNum && index <= this.secondNum;
};

module.exports = IndexListSelectorItem;
