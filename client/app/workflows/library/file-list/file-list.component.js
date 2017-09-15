
import tpl from './file-list.template.html';
import FileListController from './file-list.controller.js';

const FileListComponent = {
  controller: FileListController,
  bindings: {
    items: '<',
    parents: '<'
  },
  templateUrl: tpl
};

export default FileListComponent;
