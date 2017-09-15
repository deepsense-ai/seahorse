'use strict';

const URL = Symbol('URL');
const LIBRARY_PREFIX = Symbol('LIBRARY_PREFIX');


export default class LibraryApi {
  constructor($http, config) {
    'ngInject';

    this.$http = $http;
    this[URL] = `${config.apiHost}:${config.apiPort}/library`;
    this[LIBRARY_PREFIX] = config.libraryPrefix;
  }


  addDirectory(directoryName, parentDirectoryPath) {
    const separator = parentDirectoryPath.endsWith('/') ? '' : '/';
    const directoryPath = `${parentDirectoryPath}${separator}${directoryName}`;

    return this.$http.post(this.getResourceUrl(directoryPath));
  }


  /**
   * @returns {Promise}
   */
  getAll() {
    return this.$http
      .get(this[URL])
      .then((result) => {
        return result.data;
      });
  }


  getResourceUri(resourcePath) {
    // Dirty hack
    return `${this[LIBRARY_PREFIX]}${resourcePath}`.replace('///', '//');
  }


  getResourceUrl(resourcePath) {
    return `${this[URL]}${resourcePath}`;
  }


  removeDirectory(directoryPath) {
    return this.$http.delete(this.getResourceUrl(directoryPath));
  }


  // TODO: combine removeFile and removeDirectory into method removeResource
  //       use resourcePath as input and generate resuurce URL internaly
  removeFile(fileUrl) {
    return this.$http.delete(fileUrl);
  }


  uploadFile(file, directoryPath, progressHandler) {
    const fd = new FormData();
    const directoryUrl = this.getResourceUrl(directoryPath);

    fd.append('file', file);
    return this.$http
      .post(directoryUrl, fd, {
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
