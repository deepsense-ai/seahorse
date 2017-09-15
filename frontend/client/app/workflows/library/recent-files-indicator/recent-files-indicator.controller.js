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

require('./recent-files-indicator.less');

class RecentFilesIndicatorController {
  /* @ngInject */
  constructor($scope, LibraryModalService, LibraryService) {
    this.LibraryModalService = LibraryModalService;

    $scope.$watch(LibraryModalService.getUploadingFilesPopoverStatus, (newValue) => {
      this.recentFilesListOpen = newValue;
    });

    $scope.$watch(LibraryService.getUploadingFiles, (newValue) => {
      this.uploadingFiles = newValue.filter((value) => value.status === 'uploading');
      this.uploadedFiles = newValue.filter((value) => value.status === 'complete');
      this.allFiles = [...this.uploadingFiles, ...this.uploadedFiles];
    }, true);
  }

  toggleOpenRecentFiles() {
    this.LibraryModalService.toggleUploadingFilesPopover();
  }
}

export default RecentFilesIndicatorController;
