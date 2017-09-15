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
    this.extension = this.item.name ? this.getExtension(this.item.name) : '';
    this.canShowExtension = this.extension === 'json' || this.extension === 'csv';

    if (this.item.parents) {
      if (this.item.progress) {
        this.formatParentsForUploadedFile();
      }
    }
  }


  getExtension(fileName) {
    return fileName.substr(fileName.lastIndexOf('.') + 1);
  }


  formatParentsForUploadedFile() {
    const parents = this.item.parents.slice(1);
    const visibleParents = parents.slice(-(VISIBLE_PARENTS_COUNT + 1));

    if (parents.length > VISIBLE_PARENTS_COUNT) {
      visibleParents[0] = {
        name: '...',
        title: visibleParents[0].name,
        uri: visibleParents[0].uri
      };
    }

    this.parents = visibleParents;
  }


  formatParentsForFilteredResource() {
    const currentDirectory = this.LibraryService.getCurrentDirectory();

    if (currentDirectory.isFiltered()) {
      const parents = this.item.parents.slice(this.item.parents.indexOf(currentDirectory.directory) + 1);
      const visibleParents = parents.slice(-(VISIBLE_PARENTS_COUNT + 1));

      if (parents.length > VISIBLE_PARENTS_COUNT) {
        visibleParents[0] = {
          name: '...',
          title: visibleParents[0].name,
          uri: visibleParents[0].uri
        };
      }

      this.parents = visibleParents;
    }
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
