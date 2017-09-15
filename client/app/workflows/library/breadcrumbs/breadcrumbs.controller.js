require('./breadcrumbs.less');

const MAX_PARENT_NUMBER_VISIBLE = 4;

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
    if (parents.length > MAX_PARENT_NUMBER_VISIBLE) {
      const firstParentNotVisible = parents[parents.length - MAX_PARENT_NUMBER_VISIBLE];
      this.parents = [
        parents[0],
        {
          name: '...',
          uri: firstParentNotVisible.uri,
          title: firstParentNotVisible.name
        },
        ...parents.slice(parents.length - (MAX_PARENT_NUMBER_VISIBLE - 1), parents.length)
      ];
    } else {
      this.parents = [...parents];
    }
  }

}

export default BreadcrumbsController;
