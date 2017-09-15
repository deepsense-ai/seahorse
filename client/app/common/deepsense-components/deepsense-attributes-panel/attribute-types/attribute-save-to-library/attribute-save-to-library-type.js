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
