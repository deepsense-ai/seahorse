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

import tpl from './attribute-save-to-library-type.html';

const LIBRARY_MODE = 'write-to-file';

/* @ngInject */
function AttributeSaveToLibrary(LibraryModalService) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    link: function (scope) {
      scope.openLibrary = (params) => {
        LibraryModalService.openLibraryModal(LIBRARY_MODE, params)
          .then((result) => {
            if (result) {
              scope.parameter.value = result;
            }
          });

      };
    }
  };
}

angular.module('deepsense.attributes-panel')
  .directive('attributeSaveToLibrary', AttributeSaveToLibrary);
