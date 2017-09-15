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

import tpl from './library-connector.html';

const LIBRARY_MODE = 'read-file';

/* @ngInject */
function LibraryConnector() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    bindToController: {
      fileUri: '='
    },
    controllerAs: 'controller',
    controller: function ($scope, LibraryModalService, LibraryService) {
      const vm = this;

      vm.openLibrary = openLibrary;

      $scope.$watchGroup([() => vm.fileUri, () => LibraryService.getAll()], () => {
        const file = LibraryService.getFileByURI(vm.fileUri);
        vm.label = file ? file.name : 'Library';
      });

      function openLibrary() {
        LibraryModalService.openLibraryModal(LIBRARY_MODE)
          .then((result) => {
            if (result) {
              vm.fileUri = result.uri;
            }
          });
      }
    }
  };
}

angular.module('deepsense.attributes-panel')
  .directive('libraryConnector', LibraryConnector);
