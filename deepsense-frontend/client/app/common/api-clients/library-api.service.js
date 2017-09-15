'use strict';

/* @ngInject */
function LibraryApi($http, config) {
  const URL = `${config.apiHost}:${config.apiPort}/library`;
  const service = this;

  service.getAll = getAll;
  service.upload = upload;
  service.remove = remove;
  service.getDownloadUrlForFile = getDownloadUrlForFile;

  /**
   * @returns {Promise}
   */
  function getAll() {
    return $http.get(URL).then(function processResult(result) {
      const arr = [];
      result.data.forEach((file) => {
        const parsedFile = Object.assign(file, {
          downloadUrl: getDownloadUrlForFile(file.name)
        });
        arr.push(parsedFile);
      });
      return arr;
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
    }).then(function processResult(result) {
      return result;
    });
  }

  /**
   * @param {String} fileName
   * @returns {Promise}
   */
  function remove(fileName) {
    return $http.delete(`${URL}/${fileName}`).then(function processResult(result) {
      return result;
    });
  }

  /**
   * @param {String} fileName
   * @returns {string} Url
   */
  function getDownloadUrlForFile(fileName) {
    return URL + '/' + encodeURIComponent(fileName);
  }
}

exports.inject = function(module) {
  module.service('LibraryApiService', LibraryApi);
};
