/**
 * Copyright 2017, deepsense.ai
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

/* ngInject */
function FileUploadChange(LibraryService) {
  return {
    restrict: 'A',
    link: function (scope, element) {
      element.bind('change', (event) => {
        LibraryService.uploadFiles([...event.target.files]);
        element.val(null);
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('fileUploadChange', FileUploadChange);
};