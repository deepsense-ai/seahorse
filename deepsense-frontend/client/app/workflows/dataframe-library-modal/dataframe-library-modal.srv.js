'use strict';

/* @ngInject */
function DataframeLibraryModalService($uibModal, LibraryService) {
  const service = this;

  service.openLibraryModal = openLibraryModal;

  function openLibraryModal(canChooseDataframe) {
    return $uibModal.open({
      animation: false,
      templateUrl: 'app/workflows/dataframe-library-modal/dataframe-library-modal.html',
      controller: 'DataframeLibraryModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        canChooseDataframe: () => {
          return canChooseDataframe;
        }
      }
    }).result.then(function (result) {
      LibraryService.cleanUploadingFiles();
      return result;
    }, function () {
      LibraryService.cleanUploadingFiles();
    });

  }

}

exports.inject = function (module) {
  module.service('DataframeLibraryModalService', DataframeLibraryModalService);
};
