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

  openRecentFilesTable() {
    this.LibraryModalService.openUploadingFilesPopover();
  }

  closeRecentFilesTable() {
    this.LibraryModalService.closeUploadingFilesPopover();
  }
}

export default RecentFilesIndicatorController;
