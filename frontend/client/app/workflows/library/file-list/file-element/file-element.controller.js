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

// TODO think of refactor this controller as it's trying to resolve all cases
require('./file-element.less');

import fileTpl from './templates/file.template.html';
import directoryTpl from './templates/directory.template.html';
import parentTpl from './templates/parent.template.html';
import newDirTpl from './templates/new-directory.template.html';

const templateMap = {
  file: fileTpl,
  directory: directoryTpl,
  parent: parentTpl,
  newDir: newDirTpl
};

const COOKIE_NAME = 'DELETE_DATAFRAME_COOKIE';
const VISIBLE_PARENTS_COUNT = 2;

const ESC_CODE = 27;
const ENTER_CODE = 13;

class FileElementController {
  constructor($scope, DeleteModalService, LibraryModalService, LibraryService) {
    'ngInject';

    this.DeleteModalService = DeleteModalService;
    this.LibraryModalService = LibraryModalService;
    this.LibraryService = LibraryService;

    $scope.$watchGroup([
      () => LibraryService.getCurrentDirectory().path,
      () => LibraryService.getCurrentDirectory().isFiltered()
    ], (newValues) => {
      this.isFiltered = newValues[1];
      if (this.isFiltered && this.item.parents) {
        this.formatParentsForFilteredResource();
      }
    });
  }


  $onChanges(changes) {
    this.templateUrl = templateMap[this.item.kind];
    this.extension = this.getExtension(this.item.name);
    this.canShowExtension = this.extension === 'json' || this.extension === 'csv';

    if (this.item.parents && this.item.progress) {
      this.formatParentsForUploadedFile();
    }
  }


  onKeyDownHandler(event) {
    const keyCode = event.keyCode;
    if (keyCode === ESC_CODE) {
      event.preventDefault();
      this.LibraryModalService.hideNewDirectoryInput();
    } else if (keyCode === ENTER_CODE && (this.newDirectoryName !== '' && !this.isDirectoryNameUsed())) {
      this.saveNewDir();
    }
  }


  isDirectoryNameUsed() {
    return this.LibraryService
      .getCurrentDirectory()
      .containsDirectory(this.newDirectoryName);
  }


  getExtension(fileName) {
    if (!fileName) {
      return '';
    }
    return fileName
      .split('.')
      .pop()
      .toLowerCase();
  }


  formatParentsForUploadedFile() {
    const parents = this.item.parents.slice(1);
    this.parents = this.getVisibleParents(parents);
  }


  formatParentsForFilteredResource() {
    const currentDirectory = this.LibraryService.getCurrentDirectory();

    if (currentDirectory.isFiltered()) {
      const parents = this.item.parents.slice(this.item.parents.indexOf(currentDirectory.directory) + 1);
      this.parents = this.getVisibleParents(parents);
    }
  }


  getVisibleParents(parents) {
    const visibleParents = parents.slice(-(VISIBLE_PARENTS_COUNT + 1));

    if (parents.length > VISIBLE_PARENTS_COUNT) {
      visibleParents[0] = {
        name: '...',
        title: visibleParents[0].name,
        uri: visibleParents[0].uri
      };
    }

    return visibleParents;
  }


  goToUri(uri) {
    this.cancelAddingNewDir();
    this.LibraryModalService.closeUploadingFilesPopover();
    this.LibraryService.changeDirectory(uri);
  }


  deleteFile(file) {
    this.DeleteModalService.handleDelete(() => {
      this.LibraryService.removeFile(file)
        .then(() => {
          this.LibraryService.removeUploadingFile(file);
        });
    }, COOKIE_NAME);
  }


  deleteDirectory(directory) {
    this.DeleteModalService.handleDelete(() => {
      this.LibraryService.removeDirectory(directory);
    }, COOKIE_NAME);
  }


  saveNewDir() {
    this.LibraryService.addDirectory(this.newDirectoryName);
    this.LibraryModalService.hideNewDirectoryInput();
  }


  cancelAddingNewDir() {
    this.LibraryModalService.hideNewDirectoryInput();
  }
}

export default FileElementController;
