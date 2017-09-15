'use strict';

const COOKIE_NAME = 'DELETE_DATAFRAME_COOKIE';

/* @ngInject */
function LibraryModalCtrl($scope, $uibModalInstance, LibraryService, LibraryModalService, mode, DeleteModalService, $log) {
  const vm = this;

  vm.loading = true;
  vm.filterString = '';
  vm.uploadingFiles = [];
  vm.uploadedFiles = [];
  vm.mode = mode;
  vm.selectedItem = '';

  vm.deleteFile = deleteFile;
  vm.onSelect = onSelect;
  vm.close = close;
  vm.ok = ok;
  vm.showNewDirectoryInput = showNewDirectoryInput;

  $scope.$watch(() => LibraryService.getDirectoryContent(), (newValue) => {
    handleResults(newValue);
  });

  $scope.$watch(() => LibraryService.getSearchResults(), (newValue) => {
    vm.searchResults = newValue;
  });

  $scope.$watchGroup([() => vm.filterString, () => vm.currentDirName], () => {
    LibraryService.searchFilesInDirectory(vm.filterString);
  });

  LibraryService.fetchAll()
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
    LibraryModalService.showNewDirectoryInput();
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
