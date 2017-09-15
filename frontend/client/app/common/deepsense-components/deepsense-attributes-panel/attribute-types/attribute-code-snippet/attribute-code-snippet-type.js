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

require('NODE_MODULES/ace-webapp/src-min-noconflict/mode-sql.js');
require('NODE_MODULES/ace-webapp/src-min-noconflict/mode-python.js');
require('NODE_MODULES/ace-webapp/src-min-noconflict/mode-r.js');

require('./attribute-code-snippet-type.ctrl.js');
import tpl from './attribute-code-snippet-type.html';

/* @ngInject */
function AttributeCodeSnippetType() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: false,
    scope: {
      value: '=',
      language: '='
    },
    bindToController: true,
    controller: 'AttributeCodeSnippetTypeCtrl',
    controllerAs: 'acstCtrl'
  };
}

angular
  .module('deepsense.attributes-panel')
  .directive('attributeCodeSnippetType', AttributeCodeSnippetType);

