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

/* ngInject */
function DropzoneFileUpload(LibraryService) {
  return {
    restrict: 'A',
    link: function (scope, element) {

      element.on('dragover', stopEvents);

      element.on('dragenter', function (e) {
        stopEvents(e);
        element.addClass('drag-over');
        element.text('Drop files!');
      });

      element.on('drop', function (e) {
        stopEvents(e);
        element.removeClass('drag-over');
        element.text('Drag new files here');
        LibraryService.uploadFiles([...e.dataTransfer.files]);
      });

      element.on('dragleave', function (e) {
        stopEvents(e);
        element.removeClass('drag-over');
        element.text('Drag new files here');
      });

      function stopEvents(event) {
        event.preventDefault();
        event.stopPropagation();
      }
    }
  };
}

exports.inject = function (module) {
  module.directive('dropzoneFileUpload', DropzoneFileUpload);
};
