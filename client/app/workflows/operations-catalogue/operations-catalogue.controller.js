class OperationsCatalogueController {
  constructor($scope, OperationsCatalogueService) {
    'ngInject';
    this.categories = OperationsCatalogueService.getAllOperations();

    $scope.$watch(() => this.query, (newValue) => {
      this.isSearchMode = newValue && newValue.length > 2;
      if (this.isSearchMode) {
        const tree = {catalog: this.categories, items: []};
        this.filteredCategories = this.filterCatalog(tree, this.query);

        if (this.filteredCategories && this.filteredCategories.catalog.length > 0) {
          this.message = '';
        } else {
          this.message = 'There are no operations matching query';
        }
      }
    });
  }

  filterCatalog(tree, filterQuery) {
    const newTree = angular.copy(tree);
    newTree.catalog = _
      .chain(newTree.catalog)
      .map(c => this.filterCatalog(c, filterQuery))
      .filter(c => !_.isNull(c))
      .value();
    newTree.items = _.filter(newTree.items, (item) => {
      return item.name.toLowerCase().indexOf(filterQuery.toLowerCase()) > -1;
    });
    if (newTree.catalog.length === 0 && newTree.items.length === 0) {
      return null;
    } else {
      return newTree;
    }
  };

}

export default OperationsCatalogueController;
