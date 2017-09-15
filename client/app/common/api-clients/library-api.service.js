'use strict';

/* @ngInject */
function LibraryApi($http, config) {
  const URL = `${config.apiHost}:${config.apiPort}/library`;
  const service = this;

  service.getAll = getAll;
  service.upload = upload;
  service.remove = remove;
  service.getDownloadUrlForFile = getDownloadUrlForFile;
  service.getUriForFile = getUriForFile;

  /**
   * @returns {Promise}
   */
  function getAll() {
    return $http.get(URL).then(function processResult(result) {
      return result.data.map((file) => {
        return Object.assign(file, {
          downloadUrl: getDownloadUrlForFile(file.name),
          uri: getUriForFile(file.name)
        });
      });
    });
  }

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
