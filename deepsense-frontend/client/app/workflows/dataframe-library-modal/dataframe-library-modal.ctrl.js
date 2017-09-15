'use strict';

const COOKIE_NAME = 'DELETE_DATAFRAME_COOKIE';

/* @ngInject */
function DataframeLibraryModalCtrl($scope, $uibModalInstance, LibraryService, canChooseDataframe, DeleteModalService) {
  const vm = this;

  vm.loading = true;
  vm.filterString = '';
  vm.uploadingFiles = [];
  vm.uploadedFiles = [];
  vm.canChooseDataframe = canChooseDataframe || false;

  vm.search = search;
  vm.openFileBrowser = openFileBrowser;
  vm.fileUploaded = fileUploaded;
  vm.selectDataframe = selectDataframe;
  vm.deleteFile = deleteFile;
  vm.deleteUploadedFile = deleteUploadedFile;
  vm.close = close;

  $scope.$watch(LibraryService.getAll, () => {
    vm.dataframes = LibraryService.getAll();
    if (vm.dataframes && vm.dataframes.length === 0) {
      vm.message = 'There are no files in library. Upload files in order to use them as your dataframes.';
    } else {
      vm.message = '';
    }
  });

  $scope.$watch(LibraryService.getUploadingFiles, (newValue, oldValue) => {
    vm.uploadingFiles = newValue.filter((value) => value.status === 'uploading');
    vm.uploadedFiles = newValue.filter((value) => value.status === 'complete');
  }, true);

  LibraryService.fetchAll().then(function processResult(result) {
    vm.dataframes = result;
    vm.loading = false;
  }, function processFailure() {
    vm.loading = false;
    vm.message = 'There was an error during downloading list of files.';
  });

  function search(dataframe) {
    return !vm.filterString || dataframe.name.toLowerCase().includes(vm.filterString.toLowerCase());
  }

  function openFileBrowser() {
    document.getElementById('uploader-input').click();
  }

  function fileUploaded(files) {
    LibraryService.uploadFiles([...files]);
  }

  function selectDataframe(file) {
    if (vm.canChooseDataframe) {
      $uibModalInstance.close(file);
    }
  }

  function deleteFile(fileName) {
    DeleteModalService.handleDelete(() => {
      LibraryService.removeFile(fileName)
    }, COOKIE_NAME);
  }

  function deleteUploadedFile(fileName) {
    DeleteModalService.handleDelete(() => {
      LibraryService.removeFile(fileName).then(() => {
        return LibraryService.removeUploadingFileByName(fileName);
      })
    }, COOKIE_NAME);
  }

  function close() {
    $uibModalInstance.dismiss();
  }

}

exports.inject = function (module) {
  module.controller('DataframeLibraryModalCtrl', DataframeLibraryModalCtrl);
};
