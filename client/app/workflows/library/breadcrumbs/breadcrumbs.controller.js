require('./breadcrumbs.less');

const MAX_PARENT_NO_VISIBLE = 3;

class BreadcrumbsController {
  /* @ngInject */
  constructor($scope, LibraryService) {
    this.LibraryService = LibraryService;

    $scope.$watch(() => this.allParents, (parents) => {
      this.shortenParentsArray(parents);
    });
  }

  goToParent(uri) {
    this.LibraryService.getDirectoryContent(uri);
  }

  shortenParentsArray(parents) {
    if (parents.length > MAX_PARENT_NO_VISIBLE) {
      const firstParentNotVisible = parents[parents.length - MAX_PARENT_NO_VISIBLE];
      this.parents = [
        parents[0],
        {
          name: '...',
          uri: firstParentNotVisible.uri,
          title: firstParentNotVisible.name
        },
        ...parents.slice(parents.length - (MAX_PARENT_NO_VISIBLE - 1), parents.length)
      ];
    } else {
      this.parents = [...parents];
    }
  }

}

export default BreadcrumbsController;
