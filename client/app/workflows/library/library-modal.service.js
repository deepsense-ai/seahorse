'use strict';

import tpl from './library-modal.html';

/* @ngInject */
function LibraryModalService($uibModal) {
  const service = this;

  let isUploadingFilesPopoverOpen = false;
  let isNewDirectoryInputVisible = false;

  service.openLibraryModal = openLibraryModal;
  service.openUploadingFilesPopover = openUploadingFilesPopover;
  service.closeUploadingFilesPopover = closeUploadingFilesPopover;
  service.toggleUploadingFilesPopover = toggleUploadingFilesPopover;
  service.getUploadingFilesPopoverStatus = getUploadingFilesPopoverStatus;
  service.getNewDirectoryInputVisibility = getNewDirectoryInputVisibility;
  service.showNewDirectoryInput = showNewDirectoryInput;
  service.hideNewDirectoryInput = hideNewDirectoryInput;

  function openLibraryModal(mode, params) {
    return $uibModal.open({
      animation: false,
      templateUrl: tpl,
      size: 'lg',
      controller: 'LibraryModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        mode: () => {
          return mode;
        },
        params: () => {
          return params;
        }
      }
    }).result.then((result) => {
        closeUploadingFilesPopover();
        return result;
      })
      .catch(() => {
        closeUploadingFilesPopover();
        hideNewDirectoryInput();
      });
  }

  function openUploadingFilesPopover() {
    isUploadingFilesPopoverOpen = true;
  }

  function closeUploadingFilesPopover() {
    isUploadingFilesPopoverOpen = false;
  }

  function toggleUploadingFilesPopover() {
    isUploadingFilesPopoverOpen = !isUploadingFilesPopoverOpen;
  }

  function getUploadingFilesPopoverStatus() {
    return isUploadingFilesPopoverOpen;
  }

  function showNewDirectoryInput() {
    isNewDirectoryInputVisible = true;
  }

  function hideNewDirectoryInput() {
    isNewDirectoryInputVisible = false;
  }

  function getNewDirectoryInputVisibility() {
    return isNewDirectoryInputVisible;
  }
}

exports.inject = function (module) {
  module.service('LibraryModalService', LibraryModalService);
};
