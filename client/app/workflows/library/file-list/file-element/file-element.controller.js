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
  constructor(DeleteModalService, LibraryService) {
    this.DeleteModalService = DeleteModalService;
    this.LibraryService = LibraryService;
  }

  $onChanges(changes) {
    this.templateUrl = templateMap[this.item.kind];
  }

  goToUri(item) {
    this.LibraryService.getDirectoryContent(item.uri);
  }

  deleteFile(file) {
    this.DeleteModalService.handleDelete(() => {
      this.LibraryService.removeFile(file)
        .then(() => {
          this.LibraryService.removeUploadingFile(file);
        });
    }, COOKIE_NAME);
  }

}

export default FileElementController;
