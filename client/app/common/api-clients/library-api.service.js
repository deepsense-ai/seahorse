'use strict';

/* @ngInject */
function LibraryApi($http, config) {
  const URL = `${config.apiHost}:${config.apiPort}/library`;
  const service = this;

  service.addDirectory = addDirectory;
  service.getAll = getAll;
  service.getResourceUrl = getResourceUrl;
  service.removeFile = removeFile;
  service.uploadFile = uploadFile;

  service.upload = upload;
  service.remove = remove;
  service.getDownloadUrlForFile = getDownloadUrlForFile;
  service.getUriForFile = getUriForFile;


  function addDirectory(directoryName, parentDirectoryPath) {
    const directoryPath = parentDirectoryPath +
      (parentDirectoryPath.endsWith('/') ? '' : '/') +
      directoryName;

    return $http.post(getResourceUrl(directoryPath));
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


  // TODO: Code below: review, update, remove unused

  /**
   * @param {File} file
   * @param {Function} progressHandler
   * @returns {Promise}
   */
  function upload(file, progressHandler) {
    const fd = new FormData();
    fd.append('file', file);
    return $http.post(URL, fd, {
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

  /**
   * @param {String} fileName
   * @returns {Promise}
   */
  function remove(fileName) {
    return $http.delete(`${URL}/${fileName}`);
  }

  /**
   * @param {String} fileName
   * @returns {string} Url
   */
  function getDownloadUrlForFile(fileName) {
    return `${URL}/${encodeURIComponent(fileName)}`;
  }

  /**
   * @param {String} fileName
   * @returns {string}
   */
  function getUriForFile(fileName) {
    return config.libraryPrefix + fileName;
  }
}

exports.inject = function(module) {
  module.service('LibraryApiService', LibraryApi);
};
