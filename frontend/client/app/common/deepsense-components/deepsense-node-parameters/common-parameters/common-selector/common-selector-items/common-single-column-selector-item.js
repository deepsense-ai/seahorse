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

function SingleColumnSelectorItem(options) {
  this.type = SingleColumnSelectorItem.getType();
  this.addColumn(options.item.value);
}

SingleColumnSelectorItem.prototype = new GenericSelectorItem();
SingleColumnSelectorItem.prototype.constructor = GenericSelectorItem;

SingleColumnSelectorItem.prototype.addColumn = function addColumn(name) {
  this.column = {
    'name': name
  };
};

SingleColumnSelectorItem.prototype.serialize = function serialize() {
  return this.column.name ? {
    'type': this.type.id,
    'value': this.column.name
  } : null;
};

// TODO why not prototype?
SingleColumnSelectorItem.getType = () => {
  return {
    'id': 'column',
    'verbose': 'Select by name'
  };
};

SingleColumnSelectorItem.prototype.containsField = function(field) {
  return this.column.name === field.name;
};

module.exports = SingleColumnSelectorItem;
