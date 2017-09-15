'use strict';

import tpl from './library-modal.html';

/* @ngInject */
function LibraryModalService($uibModal, LibraryService) {
  const service = this;

  service.openLibraryModal = openLibraryModal;

  function openLibraryModal(canChooseDataframe) {
    return $uibModal.open({
      animation: false,
      templateUrl: tpl,
      size: 'lg',
      controller: 'LibraryModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        canChooseDataframe: () => {
          return canChooseDataframe;
        }
      }
    }).result.then((result) => {
      LibraryService.cleanUploadingFiles();
      return result;
    }).catch(() => {
      LibraryService.cleanUploadingFiles();
    });

  }

}

exports.inject = function (module) {
  module.service('LibraryModalService', LibraryModalService);
};
