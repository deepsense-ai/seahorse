/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

import CurrentDirectory from './current-directory';


const STATUS_UPLOADING = 'uploading';
const STATUS_ERROR = 'error';
const STATUS_COMPLETE = 'complete';

/* @ngInject */
function LibraryService($q, $log, LibraryDataConverterService, LibraryApiService) {
  const uploading = [];
  const service = this;
  const currentDirectory = new CurrentDirectory();

  let library;
  let currentDirectoryUri;
  let lastSearch = {
    directory: null,
    parrern: null,
    results: null
  };

  service.addDirectory = addDirectory;
  service.changeDirectory = changeDirectory;
  service.fetchAll = fetchAll;
  service.getAll = getAll;  // Used once in LibraryConnector
  service.getCurrentDirectory = getCurrentDirectory;
  service.getCurrentDirectoryContent = getCurrentDirectoryContent;
  service.getFileByURI = getFileByURI; // Used once in LibraryConnector
  service.getUploadingFiles = getUploadingFiles;
  service.isUploadingInProgress = isUploadingInProgress;
  service.removeDirectory = removeDirectory;
  service.removeFile = removeFile;
  service.removeUploadingFile = removeUploadingFile;
  service.setFilter = setFilter;
  service.uploadFiles = uploadFiles;
  service.doesDirectoryAlreadyExists = doesDirectoryAlreadyExists;

  fetchAll();


  /**
   * @param {String} directoryName
   * @returns {Promise} Promise with parsed data from API
   */
  function addDirectory(directoryName) {
    $log.info(`LibraryService.addDirectory(${directoryName})`);

    return LibraryApiService
      .addDirectory(directoryName, currentDirectory.path)
      .then((result) => {
        service.fetchAll();
        return result;
      });
  }


  function changeDirectory(directoryUri) {
    const newDirectory = library.get(directoryUri) || library.getRootDirectory();
    currentDirectory.changeTo(newDirectory);
    // TODO: remove
    currentDirectoryUri = currentDirectory.uri;
  }


  /**
   * Fetches library from the server to local object
   * @returns {Promise}
   */
  function fetchAll() {
    return LibraryApiService
      .getAll()
      .then((results) => {
        library = LibraryDataConverterService.decodeResponseData(results);
        changeDirectory(currentDirectory.uri);

        return library;
      });
  }


  // TODO: Do we need this? Used once in LibraryConnector in $watchGroun
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


  function getCurrentDirectory() {
    return currentDirectory;
  }


  function getCurrentDirectoryContent() {
    return currentDirectory.items;
  }


  // TODO: Do we need this? Used once in LibraryConnector
  /**
   * @param {String} uri
   * @returns {FileObject}
   */
  function getFileByURI(uri) {
    const parsedUri = /(library:\/\/)(.*)/.exec(uri);
    if (!parsedUri) {
      return false;
    }

    const [fileName, items] = (
      (parts) => (
        (prefix, path) => [path.pop(), library.get(`${prefix}${path.join('/')}`).items]
      )(parts[0], parts[1].split('/'))
    )(parsedUri.slice(1));

    return _.find(items, {name: fileName});
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
   * @returns {Bool} Flag indicating whether uploading
   * is in progress:
   */
  function isUploadingInProgress() {
    return uploading
      .filter((value) => value.status === 'uploading')
      .length > 0;
  }


  /**
   * @param {Object} directory
   * @returns {Promise} Promise with parsed data from API
   */
  function removeDirectory(directory) {
    $log.info(`LibraryService.removeDirectory(${directory})`);

    return LibraryApiService
      .removeDirectory(directory.path)
      .then((result) => {
        service.fetchAll();
        return result;
      });
  }


  /**
   * @param {Object} file
   * @returns {Promise} Promise with parsed data from API
   */
  function removeFile(file) {
    $log.info(`LibraryService.removeFile(${file})`);

    return LibraryApiService
      .removeFile(file.downloadUrl)
      .then((result) => {
        service.fetchAll();
        return result;
      });
  }


  /**
   * @returns {Array} with removed files from uploading list
   */
  function removeUploadingFile(file) {
    return _.remove(uploading, uploadedFile => uploadedFile.uri === file.uri);
  }


  function setFilter(filter) {
    $log.info(`LibraryService.setFilter(${filter})`);

    currentDirectory.setFilter(filter);

    $log.info(`> filter set to [${currentDirectory.filter}]`);
  }

  /**
   * Uploads the file to the server and tracks the upload progress. Handles server errors.
   * @param {File} file from HTML5 FileAPI
   * @returns {Promise}
   */
  function uploadFile(file) {
    $log.info(`LibraryService.uploadFile(${file})`);

    const uploadingFile = LibraryDataConverterService.makeLibraryFile({
        kind: 'file',
        name: file.name
      },
      currentDirectory.directory,
      {
        progress: 0,
        status: STATUS_UPLOADING
      }
    );

    const progressHandler = function (progress) {
      uploadingFile.progress = progress;
      if (progress === 100) {
        uploadingFile.status = STATUS_COMPLETE;
      } else {
        uploadingFile.status = STATUS_UPLOADING;
      }
    };

    const alreadyUploadedIndex = uploading.findIndex((uploaded) => uploaded.path === uploadingFile.path);

    if (alreadyUploadedIndex > -1) {
      uploading.splice(alreadyUploadedIndex, 1);
    }
    uploading.push(uploadingFile);

    return LibraryApiService
      .uploadFile(file, currentDirectory.path, progressHandler)
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
   * @param {Array} files to be uploaded
   * @returns {Promise}
   */
  function uploadFiles(files) {
    $log.info(`LibraryService.uploadFiles(${files})`);

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
   * @param {String} name of directory to be created
   * @returns {Bool}
   */
  function doesDirectoryAlreadyExists(directoryName) {
    return currentDirectory.containsDirectory(directoryName);
  }

}


exports.inject = function (module) {
  module.service('LibraryService', LibraryService);
};
