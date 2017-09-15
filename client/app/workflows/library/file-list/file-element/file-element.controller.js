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

class FileElementController {
  /* @ngInject */
  constructor(DeleteModalService, LibraryModalService, LibraryService) {
    this.DeleteModalService = DeleteModalService;
    this.LibraryModalService = LibraryModalService;
    this.LibraryService = LibraryService;
  }

  $onChanges(changes) {
    this.templateUrl = templateMap[this.item.kind];
    this.extension = this.item.name ? this.getExtension(this.item.name) : '';
    this.canShowExtension = this.extension === 'json' || this.extension === 'csv';

    if (this.item.parents) {
      this.formatParents(this.item.parents);
    }
  }

  getExtension(fileName) {
    return fileName.substr(fileName.lastIndexOf('.') + 1);
  }

  formatParents(parents) {
    const lastTwoParents = parents.slice(Math.max(parents.length - 2, 1));
    if (parents.length > 2) {
      this.parents = [
        {
          name: '...',
          uri: parents[parents.length - 3].uri
        },
        ...lastTwoParents
      ];
    }
  }

  goToUri(uri) {
    this.cancelAddingNewDir();
    this.LibraryModalService.closeUploadingFilesPopover();
    this.LibraryService.getDirectoryContent(uri);
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
