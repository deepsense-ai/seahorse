/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
