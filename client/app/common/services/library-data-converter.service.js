'use strict';


/* @ngInject */
function LibraryDataConverter(config, LibraryApiService) {
  const service = this;

  service.decodeResponseData = decodeResponseData;
  service.makeLibraryFile = makeLibraryFile;


  function decodeResponseData(data) {
    const library = new Map();
    const ROOT_DIRECTORY_URI = addDirectoryToLibrary(library, data).uri;

    library.getRootDirectory = function getRootDirectory() {
      return library.get(ROOT_DIRECTORY_URI);
    };

    return library;
  }


  function addDirectoryToLibrary(library, directoryData, parentDirectory) {
    const directory = makeLibraryDirectory(directoryData, parentDirectory);

    library.set(directory.uri, directory);

    directory.items = directoryData.children.map((child) => {
      if (child.kind === 'file') {
        return makeLibraryFile(child, directory);
      } else {
        return addDirectoryToLibrary(library, child, directory);
      }
    });

    return directory;
  }


  function makeLibraryResource(resourceData = {}, parentDirectory = null) {
    return Object.create({
      isDirectory: function isDirectory() {
        return this.kind === 'directory';
      },
      isFile: function isFile() {
        return this.kind === 'file';
      },
      isRoot: function isRoot() {
        return this.isDirectory() && !this.parent;
      },
      toString: function toString() {
        return this.path;
      }
    }, {
      kind: {value: resourceData.kind, enumerable: true},
      name: {value: resourceData.name, enumerable: true},
      parent: {value: parentDirectory, enumerable: true},
      parents: {
        get: function getParents() {
          if (this.isRoot()) {
            return [];
          }
          return [...this.parent.parents, this.parent];
        }
      },
      path: {
        get: function getPath() {
          if (this.isRoot()) {
            return '/';
          }
          if (this.parent.isRoot()) {
            return `/${this.name}`;
          }
          return `${this.parent.path}/${this.name}`;
        }
      },
      // TODO: remove uri property, use path instead
      uri: {
        get: function () {
          // Dirty hack, but uri will be removed anyway
          return `${config.libraryPrefix}${this.path}`.replace('///', '//');
        }
      }
    });
  }


  function makeLibraryDirectory(directoryData, parentDirectory) {
    return makeLibraryResource(directoryData, parentDirectory);
  }


  function makeLibraryFile(fileData, parentDirectory, extraParams) {
    return Object.assign(
      Object.create(makeLibraryResource(fileData, parentDirectory), {
        downloadUrl: {
          get: function () {
            return LibraryApiService.getResourceUrl(this.path);
          }
        }
      }),
      extraParams
    );
  }

}


exports.inject = function(module) {
  module.service('LibraryDataConverterService', LibraryDataConverter);
};
