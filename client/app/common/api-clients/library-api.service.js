'use strict';

/* @ngInject */
function LibraryApi($http, config) {
  const URL = `${config.apiHost}:${config.apiPort}/library`;
  const service = this;

  service.addDirectory = addDirectory;
  service.removeDirectory = removeDirectory;
  service.getAll = getAll;
  service.getResourceUri = getResourceUri;
  service.getResourceUrl = getResourceUrl;
  service.removeFile = removeFile;
  service.uploadFile = uploadFile;


  function addDirectory(directoryName, parentDirectoryPath) {
    const directoryPath = parentDirectoryPath +
      (parentDirectoryPath.endsWith('/') ? '' : '/') +
      directoryName;

    return $http.post(getResourceUrl(directoryPath));
  }


  function removeDirectory(directoryPath) {
    return $http.delete(getResourceUrl(directoryPath));
  }


  /**
   * @returns {Promise}
   */
  function getAll() {
    return $http.get(URL)
      .then((result) => {
        return result.data;
      });
  }


  function getResourceUri(resourcePath) {
    // Dirty hack, but uri will be removed anyway
    return `${config.libraryPrefix}${resourcePath}`.replace('///', '//');
  }


  function getResourceUrl(resourcePath) {
    return `${URL}${resourcePath}`;
  }


  function removeFile(fileUrl) {
    return $http.delete(fileUrl);
  }


  function uploadFile(file, directory, progressHandler) {
    const fd = new FormData();
    const directoryUrl = _.compact([URL, directory]).join('/');

    fd.append('file', file);
    return $http.post(directoryUrl, fd, {
      transformRequest: angular.identity,
      headers: {'Content-Type': undefined},
      uploadEventHandlers: {
        progress: function (param) {
          const uploadProgress = Math.ceil(param.loaded / param.total * 100);
          progressHandler(uploadProgress);
        }
      }
    });
  }
}


exports.inject = function (module) {
  module.service('LibraryApiService', LibraryApi);
};
