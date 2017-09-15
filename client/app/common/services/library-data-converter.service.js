'use strict';


/* @ngInject */
function LibraryDataConverter(config, LibraryApiService) {
  const service = this;
  const Resource = Object.create({}, {
    uri: {
      get: function () {
        return `${config.libraryPrefix}${this.path}`;
      }
    }
  });


  service.decodeResponseData = decodeResponseData;
  service.makeItem = makeItem;

  function decodeResponseData(data) {
    let library = new Map();

    addDirectoryToLibrary(library, data);

    return library;
  }


  function addDirectoryToLibrary(library, directoryData, parentDirectory) {
    let directory = makeDirectory(directoryData, parentDirectory);

    if (directory.root) {
      library.getRootDirectory = function () {
        return this.get(directory.uri);
      };
    }

    library.set(directory.uri, directory);

    directory.items = directoryData.children.map((child) => {
      if (child.kind === 'file') {
        return makeItem(child, directory);
      } else {
        return addDirectoryToLibrary(library, child, directory);
      }
    });

    return directory;
  }


  function makeDirectory(directory, parentDirectory) {
    return Object.create(Resource, {
      root: {value: !parentDirectory},
      kind: {value: directory.kind, enumerable: true},
      name: {value: directory.name, enumerable: true},
      path: {value: makeResourcePath(directory, parentDirectory), enumerable: true},
      parents: {value: getParentsList(parentDirectory), enumerable: true}
    });
  }


  function makeItem(child, parentDirectory, extraParams) {
    return Object.assign(
      Object.create(Resource, {
        kind: {value: child.kind, enumerable: true},
        name: {value: child.name, enumerable: true},
        path: {value: makeResourcePath(child, parentDirectory), enumerable: true},
        downloadUrl: {
          get: function () {
            return `${LibraryApiService.URL}/${this.path}`;
          }
        }
      }),
      extraParams
    );
  }


  function makeResourcePath(resource, parentDirectory) {
    let path = '';

    if (parentDirectory) {
      path = resource.name;
      if (!parentDirectory.root) {
        path = `${parentDirectory.path}/${path}`;
      }
    }

    return path;
  }


  function getParentsList(directory) {
    if (!directory) {
      return [];
    }

    return [...directory.parents, {
      name: directory.name,
      uri: directory.uri
    }];
  }
}


exports.inject = function(module) {
  module.service('LibraryDataConverterService', LibraryDataConverter);
};
