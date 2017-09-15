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

require('./attribute-code-snippet-type-modal/attribute-code-snippet-type-modal.ctrl.js');

import tpl from './attribute-code-snippet-type-modal/attribute-code-snippet-type-modal.html';

/* @ngInject */
function AttributeCodeSnippetTypeCtrl($uibModal) {

  var that = this;

  that.editInWindow = function() {
    var modalInstance = $uibModal.open({
      animation: true,
      backdrop: 'static',
      templateUrl: tpl,
      controller: 'AttributeCodeSnippetTypeModalCtrl',
      controllerAs: 'acstmCtrl',
      size: 'lg',
      resolve: {
        codeSnippet: function () {
          return {
            code: that.value,
            language: that.language.toLowerCase()
          };
        }
      }
    });

    modalInstance.result.then(function (modifiedCode) {
      if(that.value !== modifiedCode) {
        that.value = modifiedCode;
      }
    }, function () {});
  };

}

angular
  .module('deepsense.attributes-panel')
  .controller('AttributeCodeSnippetTypeCtrl', AttributeCodeSnippetTypeCtrl);
