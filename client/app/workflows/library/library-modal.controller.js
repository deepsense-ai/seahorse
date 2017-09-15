'use strict';

const COOKIE_NAME = 'DELETE_DATAFRAME_COOKIE';
const TITLE_MAP = {
  'read-file': 'Select data frame',
  'write-to-file': 'Write data frame'
};

/* @ngInject */
function LibraryModalCtrl($scope, $uibModalInstance, LibraryService, LibraryModalService, mode, DeleteModalService) {
  const vm = this;

  vm.loading = true;
  vm.filterString = '';
  vm.uploadingFiles = [];
  vm.uploadedFiles = [];
  vm.mode = mode;
  vm.selectedItem = '';
  vm.title = TITLE_MAP[mode] || 'Library';

  vm.deleteFile = deleteFile;
  vm.onSelect = onSelect;
  vm.close = close;
  vm.ok = ok;
  vm.showNewDirectoryInput = showNewDirectoryInput;


  $scope.$watch(() => LibraryService.getCurrentDirectoryContent(), () => {
    handleResults(LibraryService.getCurrentDirectory());
  });

  $scope.$watch(() => vm.filterString, (newFilter) => {
    LibraryService.setFilter(newFilter);
  });


  LibraryService
    .fetchAll()
    .then(() => {
      vm.loading = false;
    })
    .catch(() => {
      vm.loading = false;
      vm.message = 'There was an error during downloading list of files.';
    });


  function deleteFile(file) {
    DeleteModalService.handleDelete(() => {
      LibraryService.removeFile(file)
        .then(() => {
          LibraryService.removeUploadingFile(file);
        });
    }, COOKIE_NAME);
  }


  function close() {
    $uibModalInstance.dismiss();
  }


  function showNewDirectoryInput() {
    const isUploadingFilesPopoverOpen = LibraryModalService.getUploadingFilesPopoverStatus();
    if (!isUploadingFilesPopoverOpen) {
      LibraryModalService.showNewDirectoryInput();
    }
  }


  function handleResults(result) {
    if (!result) {
      return;
    }

    vm.items = result.items;
    vm.parents = result.parents;
    vm.currentDirName = result.name;
    vm.currentDirUri = result.uri;
  }


  function onSelect(item) {
    if (vm.mode === 'read-file') {
      $uibModalInstance.close(item);
    } else if (vm.mode === 'write-to-file') {
      vm.selectedItem = item.name;
    }
  }


  function ok() {
    $uibModalInstance.close(`${vm.currentDirUri}/${vm.selectedItem}`.replace('///', '//'));
  }
}

exports.inject = function (module) {
  module.controller('LibraryModalCtrl', LibraryModalCtrl);
};
