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


/* @ngInject */
function LibraryDataConverter(LibraryApiService) {
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
          return LibraryApiService.getResourceUri(this.path);
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
