require('./file-element.less');

import fileTpl from './templates/file.template.html';
import directoryTpl from './templates/directory.template.html';
import parentTpl from './templates/parent.template.html';

const templateMap = {
  file: fileTpl,
  directory: directoryTpl,
  parent: parentTpl
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

    if (this.item.parents) {
      this.formatParents(this.item.parents);
    }
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

}

export default FileElementController;
