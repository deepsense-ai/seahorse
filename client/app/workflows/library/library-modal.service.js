'use strict';

import tpl from './library-modal.html';

/* @ngInject */
function LibraryModalService($uibModal) {
  const service = this;

  let isUploadingFilesPopoverOpen = false;

  service.openLibraryModal = openLibraryModal;
  service.openUploadingFilesPopover = openUploadingFilesPopover;
  service.closeUploadingFilesPopover = closeUploadingFilesPopover;
  service.getUploadingFilesPopoverStatus = getUploadingFilesPopoverStatus;

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
      return result;
    });
  }

  function openUploadingFilesPopover() {
    isUploadingFilesPopoverOpen = true;
  }

  function closeUploadingFilesPopover() {
    isUploadingFilesPopoverOpen = false;
  }

  function getUploadingFilesPopoverStatus() {
    return isUploadingFilesPopoverOpen;
  }
}

exports.inject = function (module) {
  module.service('LibraryModalService', LibraryModalService);
};
