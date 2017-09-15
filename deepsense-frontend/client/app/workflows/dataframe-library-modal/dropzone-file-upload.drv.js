'use strict';

/* ngInject */
function DropzoneFileUpload() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {

      element.on('dragover', function (e) {
        stopEvents(e);
      });

      element.on('dragenter', function (e) {
        stopEvents(e);
        element.addClass('drag-over');
        element[0].innerText = 'Drop files!';
      });

      element.on('drop', function (e) {
        stopEvents(e);
        element.removeClass('drag-over');
        element[0].innerText = 'Drag new files here';
        const onChangeHandler = scope.$eval(attrs.dropzoneFileUpload);
        onChangeHandler(e.dataTransfer.files);
      });

      element.on('dragleave', function (e) {
        stopEvents(e);
        element.removeClass('drag-over');
        element[0].innerText = 'Drag new files here';
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
