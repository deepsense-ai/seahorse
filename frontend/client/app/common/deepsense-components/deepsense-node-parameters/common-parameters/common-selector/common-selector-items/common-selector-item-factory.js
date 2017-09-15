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

let SingleColumnSelectorItem = require('./common-single-column-selector-item.js');
let ColumnListSelectorItem = require('./common-column-list-selector-item.js');
let SingleIndexSelectorItem = require('./common-single-index-selector-item.js');
let IndexListSelectorItem = require('./common-index-list-selector-item.js');
let TypeListSelectorItem = require('./common-type-list-selector-item.js');

let selectorItemConstructors = {
  'column': SingleColumnSelectorItem,
  'columnList': ColumnListSelectorItem,
  'index': SingleIndexSelectorItem,
  'indexRange': IndexListSelectorItem,
  'typeList': TypeListSelectorItem
};

let SelectorItemFactory = {
  createItem(value) {
    if (_.isUndefined(value) || _.isNull(value)) {
      return null;
    } else {
      let Constructor = selectorItemConstructors[value.type];
      if (_.isUndefined(Constructor)) {
        return null;
      } else {
        return new Constructor({'item': value});
      }
    }
  },
  getAllItemsTypes() {
    return {
      'singleSelectorItems': [
        SingleColumnSelectorItem.getType(),
        SingleIndexSelectorItem.getType()
      ],
      'multipleSelectorItems': [
        TypeListSelectorItem.getType(),
        ColumnListSelectorItem.getType(),
        IndexListSelectorItem.getType()
      ]
    };
  }
};

module.exports = SelectorItemFactory;
