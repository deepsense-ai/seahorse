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
