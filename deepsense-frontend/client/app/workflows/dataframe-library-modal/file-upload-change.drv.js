'use strict';

/* ngInject */
function FileUploadChange() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      const fileUploadHandler = scope.$eval(attrs.fileUploadChange);
      element.bind('change', (event) => {
        fileUploadHandler(event.target.files);
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('fileUploadChange', FileUploadChange);
};
