'use strict';

const STATUS_UPLOADING = 'uploading';
const STATUS_ERROR = 'error';
const STATUS_COMPLETE = 'complete';

/* @ngInject */
function LibraryService($q, $log, LibraryApiService) {
  const uploading = [];
  const service = this;

  let library;

  service.getAll = getAll;
  service.fetchAll = fetchAll;
  service.uploadFile = uploadFile;
  service.uploadFiles = uploadFiles;
  service.removeFile = removeFile;
  service.getFileByURI = getFileByURI;
  service.getUploadingFiles = getUploadingFiles;
  service.cleanUploadingFiles = cleanUploadingFiles;
  service.removeUploadingFileByName = removeUploadingFileByName;

  /**
   * Fetches library from the server to local object
   * @returns {Promise}
   */
  function fetchAll() {
    return LibraryApiService.getAll().then((results) => {
      library = results;
      return library;
    });
  }

  /**
   * Exposes library to the rest of the application.
   * This functions should be watched by the controllers
   * File returned from API will have a format:
   * {
   *    name: "FileName",
   *    downloadUrl: "http://address-to-file/FileName" - address for download
   *    uri: "myLibrary://file" - address for API
   *
   * }
   * @returns {Object|undefined}
   */
  function getAll() {
    return library;
  }


  /**
   * @param {Array} files to be uploaded
   * @returns {Promise}
   */
  function uploadFiles(files) {
    let promisesArray = [];
    if (angular.isArray(files)) {
      promisesArray = files.map((file) => {
        return uploadFile(file);
      });
    } else {
      $log.error('FilesList is not an array');
    }
    return $q.all(promisesArray);
  }

  /**
   * Uploads the file to the server and tracks the upload progress. Handles server errors.
   * @param {File} file from HTML5 FileAPI
   * @returns {Promise}
   */
  function uploadFile(file) {
    const uploadingFile = {
      name: file.name,
      progress: 0,
      status: STATUS_UPLOADING
    };
    const progressHandler = function (progress) {
      uploadingFile.progress = progress;
      if (progress === 100) {
        uploadingFile.status =  STATUS_COMPLETE;
        uploadingFile.downloadUrl = LibraryApiService.getDownloadUrlForFile(uploadingFile.name);
        uploadingFile.uri = LibraryApiService.getUriForFile(uploadingFile.name);
      } else {
        uploadingFile.status = STATUS_UPLOADING;
      }
    };

    uploading.push(uploadingFile);

    return LibraryApiService.upload(file, progressHandler)
      .then((result) => {
        service.fetchAll();
        return result;
      }, (error) => {
        uploadingFile.status = STATUS_ERROR;
        $log.error('Uplading failed for file ', file, error);
        throw error;
      });
  }

  /**
   * @param {String} fileName
   * @returns {Promise} Promise with parsed data from API
   */
  function removeFile(fileName) {
    return LibraryApiService.remove(fileName).then((result) => {
      service.fetchAll();
      return result;
    });
  }

  /**
   * @param {String} uri
   * @returns {FileObject}
   */
  function getFileByURI(uri) {
    return _.find(library, (file) => file.uri === uri);
  }

  /**
   * @returns {Array} with all files with pending uploads
   * Each upload will have format as below:
   * {
   *    name: "FileName",
   *    progress: 0-100,
   *    status: 'uploading'|'complete'|'error'
   *    (if complete) downloadUrl: 'urlencoded/url/to/file'
   * }
   */
  function getUploadingFiles() {
    return uploading;
  }

  /**
   * Manages the uploading files array to keep only uploads in progress.
   * @returns {Array} with removed files from uploading list
   */
  function cleanUploadingFiles() {
    return _.remove(uploading, (file) => {
      return file.status === STATUS_COMPLETE || file.status === STATUS_ERROR;
    });
  }

  /**
   * @returns {Array} with removed files from uploading list
   */
  function removeUploadingFileByName(name) {
    return _.remove(uploading, (file) => {
      return file.name === name;
    });
  }
}

exports.inject = function(module) {
  module.service('LibraryService', LibraryService);
};

