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
