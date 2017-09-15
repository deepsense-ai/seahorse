'use strict';

const CAN_SELECT_DATAFRAME = true;

/*@ngInject*/
function LibraryConnector() {
  return {
    restrict: 'E',
    templateUrl: 'app/common/deepsense-components/deepsense-attributes-panel/attribute-types/attribute-load-from-library/library-connector/library-connector.html',
    replace: true,
    bindToController: {
      fileUri: '='
    },
    controllerAs: 'controller',
    controller: function ($scope, DataframeLibraryModalService, LibraryService) {
      const vm = this;

      vm.openLibrary = openLibrary;

      $scope.$watch(() => vm.fileUri, (newValue) => {
        const file = LibraryService.getFileByURI(newValue);
        vm.label = file ? file.name : 'Library';
      });

      function openLibrary() {
        DataframeLibraryModalService.openLibraryModal(CAN_SELECT_DATAFRAME).then((result) => {
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
