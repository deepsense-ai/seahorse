'use strict';

const STATUS_UPLOADING = 'uploading';
const STATUS_ERROR = 'error';
const STATUS_COMPLETE = 'complete';

/* @ngInject */
function LibraryService($q, $log, LibraryDataConverterService, LibraryApiService, config) {
  const uploading = [];
  const service = this;

  let library;
  let currentDirectoryUri;
  let lastSearch = {
    directory: null,
    parrern: null,
    results: null
  };

  service.cleanUploadingFiles = cleanUploadingFiles;
  service.fetchAll = fetchAll;
  service.getDirectoryContent = getDirectoryContent;
  service.getFileByURI = getFileByURI;
  service.getRootDirectoryContent = getRootDirectoryContent;
  service.getSearchResults = getSearchResults;
  service.getUploadingFiles = getUploadingFiles;
  service.removeFile = removeFile;
  service.removeUploadingFile = removeUploadingFile;
  service.searchFilesInDirectory = searchFilesInDirectory;
  service.uploadFile = uploadFile;
  service.uploadFiles = uploadFiles;


  service.getAll = getAll;

  fetchAll();

  /**
   * Fetches library from the server to local object
   * @returns {Promise}
   */
  function fetchAll() {
    return LibraryApiService
      .getAll()
      .then((results) => {
        library = LibraryDataConverterService.decodeResponseData(results);

        if (lastSearch.directory) {
          searchFilesInDirectory(lastSearch.pattern, lastSearch.directoryUri);
        }

        return library;
      });
  }


  /**
   * Return directory and its items
   * @param  {String} directoryUri  uri of desired directory
   * @return {Object} directory in library
   */
  function getDirectoryContent(directoryUri = currentDirectoryUri) {
    if (directoryUri) {
      currentDirectoryUri = directoryUri;
      return library.get(directoryUri);
    }
    return getRootDirectoryContent();
  }


  /**
   * Return root directory and its items
   * @return {Object}  root directory in library
   */
  function getRootDirectoryContent() {
    let directory = library.getRootDirectory();
    currentDirectoryUri = directory.uri;

    return directory;
  }


  function searchFilesInDirectory(pattern, directoryUri = currentDirectoryUri) {
    let directory = getDirectoryContent(directoryUri);
    let uriPrefix = directory.uri + (directory.root ? '' : '/');
    let uriPrefixLength = uriPrefix.length;
    let results = [];

    function getMatchedFiles(directory) {
      return directory.items.reduce((items, item) => {
        if (item.kind === 'file' && item.name.includes(pattern)) {
          items.push(item);
        }
        return items;
      }, []);
    }

    results.push({
      name: '',
      items: getMatchedFiles(directory)
    });
    library.forEach((subDir, subDirUri) => {
      if (subDirUri !== uriPrefix && subDirUri.startsWith(uriPrefix)) {
        let items = getMatchedFiles(subDir);
        if (items.length) {
          results.push({
            name: subDirUri.slice(uriPrefixLength),
            items: items
          });
        }
      }
    });

    lastSearch.pattern = pattern;
    lastSearch.directory = directory;
    lastSearch.results = results;

    return results;
  }


  function getSearchResults() {
    return lastSearch.results;
  }


  /**
   * @param {String} fileUrl
   * @returns {Promise} Promise with parsed data from API
   */
  function removeFile(file) {
    return LibraryApiService
      .removeFile(file.downloadUrl)
      .then((result) => {
        service.fetchAll();
        return result;
      });
  }


  /**
   * Uploads the file to the server and tracks the upload progress. Handles server errors.
   * @param {File} file from HTML5 FileAPI
   * @returns {Promise}
   */
  function uploadFile(file) {
    const workingDirectory = getDirectoryContent();
    const uploadingFile = LibraryDataConverterService.makeItem({
        kind: 'file',
        name: file.name
      },
      getDirectoryContent(),
      {
        progress: 0,
        status: STATUS_UPLOADING
      }
    );

    const progressHandler = function (progress) {
      uploadingFile.progress = progress;
      if (progress === 100) {
        uploadingFile.status =  STATUS_COMPLETE;
      } else {
        uploadingFile.status = STATUS_UPLOADING;
      }
    };

    uploading.push(uploadingFile);

    return LibraryApiService
      .uploadFile(file, workingDirectory.path, progressHandler)
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
   * @returns {Array} with removed files from uploading list
   */
  function removeUploadingFile(file) {
    return _.remove(uploading, uploadedFile => uploadedFile.uri === file.uri);
  }


  // TODO: Code below: review, update, remove unused

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
}


exports.inject = function(module) {
  module.service('LibraryService', LibraryService);
};
