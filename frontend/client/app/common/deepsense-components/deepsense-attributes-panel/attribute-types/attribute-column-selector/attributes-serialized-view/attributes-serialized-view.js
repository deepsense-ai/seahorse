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

import tpl from './attributes-serialized-view.html';

/* @ngInject */
function AttributesSerializedView() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    link: function (scope) {
      _.assign(scope, {
        getNamesList() {
          let columnListObject = scope.getItemsThisTypeOrDefault('columnList')[0];
          return columnListObject && columnListObject.columns;  // TODO shorten when there is a lot of names
        },
        getIndexList() {
          return scope.getItemsThisTypeOrDefault('indexRange');
        },
        getTypesList() {
          let typesList = scope.getItemsThisTypeOrDefault('typeList')[0];
          if (typesList && typesList.types) {
            return _.some(_.values(typesList.types)) ? typesList.types : null;
          }
        },
        getName() {
          let nameObj = scope.getItemsThisTypeOrDefault('column')[0];
          return nameObj && nameObj.column.name;
        },
        getIndex() {
          let nameObj = scope.getItemsThisTypeOrDefault('index')[0];

          return nameObj && nameObj.firstNum >= 0 && !_.isNull(nameObj.firstNum) ?
            nameObj.firstNum : null;
        },
        isValueEmpty() {
          return (scope.parameter.schema.isSingle || !scope.parameter.excluding) && _.isEmpty(scope.parameter.items);
        },
        getSingleValueOrDefault() {
          if (this.isValueEmpty()) {
            return scope.parameter.defaultItems[0];
          } else {
            return scope.parameter.items[0];
          }
        },
        isExcluding() {
          return scope.parameter.excluding || (this.isValueEmpty() && scope.parameter.defaultExcluding);
        },
        isIncluding() {
          return !this.isExcluding();
        }
      });
    }
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributesSerializedView', AttributesSerializedView);
