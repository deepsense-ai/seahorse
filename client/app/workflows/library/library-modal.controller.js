'use strict';

const COOKIE_NAME = 'DELETE_DATAFRAME_COOKIE';

/* @ngInject */
function LibraryModalCtrl($scope, $uibModalInstance, LibraryService, mode, DeleteModalService) {
  const vm = this;

  vm.loading = true;
  vm.filterString = '';
  vm.uploadingFiles = [];
  vm.uploadedFiles = [];
  vm.mode = mode;

  vm.openFileBrowser = openFileBrowser;
  vm.onFileSelectedHandler = onFileSelectedHandler;
  vm.getFilesForUri = getFilesForUri;
  vm.goToParentDirectory = goToParentDirectory;
  vm.deleteFile = deleteFile;
  vm.deleteUploadedFile = deleteUploadedFile;
  vm.onSelect = onSelect;
  vm.close = close;

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

  function openFileBrowser() {
    document.getElementById('uploader-input').click();
  }

  function onFileSelectedHandler(files) {
    LibraryService.uploadFiles([...files]);
  }

  function getFilesForUri(uri) {
    LibraryService.getDirectoryContent(uri);
  }

  function goToParentDirectory() {
    vm.getFilesForUri(_.last(vm.parents).uri);
  }

  function deleteFile(file) {
    DeleteModalService.handleDelete(() => {
      LibraryService.removeFile(file)
        .then(() => {
          LibraryService.removeUploadingFile(file);
        });
    }, COOKIE_NAME);
  }

  function deleteUploadedFile(file) {
    DeleteModalService.handleDelete(() => {
      LibraryService.removeFile(file).then(() => {
        LibraryService.removeUploadingFile(file);
      });
    }, COOKIE_NAME);
  }

  function close() {
    $uibModalInstance.dismiss();
  }

  function handleResults(result) {
    if (!result) {
      return;
    }

    vm.items = result.items;
    vm.parents = result.parents;
    vm.currentDirName = result.name;
  }

  function onSelect(item) {
    if (vm.mode === 'read-file') {
      $uibModalInstance.close(item);
    }
  }
}

exports.inject = function (module) {
  module.controller('LibraryModalCtrl', LibraryModalCtrl);
};
