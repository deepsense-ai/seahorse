require('./file-list.less');

class FileListController {
  /* @ngInject */
  constructor($scope, LibraryModalService) {
    this.mode = 'editable';

    $scope.$watch(() => LibraryModalService.getNewDirectoryInputVisibility(), (newValue) => {
      this.newDir = newValue;
      this.newDirItem = {
        kind: 'newDir'
      };
    });

    $scope.$watch(() => this.parents, (parents) => {
      if (parents && parents.length > 0) {
        const lastParent = parents[parents.length - 1];
        this.parent = {
          name: '..',
          uri: lastParent.uri,
          kind: 'parent'
        };
      } else {
        this.parent = null;
      }
    });
  }

  $onChanges(params) {
    if (params.mode && params.mode.currentValue) {
      this.mode = params.mode.currentValue;
    }
  }

}

export default FileListController;
