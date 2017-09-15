class OperationsCatalogueController {
  constructor($scope) {
    'ngInject';

    $scope.$watch(() => this.query, (newValue) => {
      this.isSearchMode = newValue && newValue.length > 2;
      if (this.isSearchMode) {
        const tree = {catalog: this.categories, items: []};
        this.filteredCategories = this.filterCatalog(tree, this.query);
      }
    });
  }

  //TODO use Operation.filterCatalog(fn)
  filterCatalog(tree, filterQuery) {
    const newTree = angular.copy(tree);
    newTree.catalog = _
      .chain(newTree.catalog)
      .map(c => this.filterCatalog(c, filterQuery))
      .filter(c => !_.isNull(c))
      .value();
    newTree.items = _.filter(newTree.items, (item) => {
      return item.name.toLowerCase().includes(filterQuery.toLowerCase());
    });
    if (newTree.catalog.length === 0 && newTree.items.length === 0) {
      return null;
    } else {
      return newTree;
    }
  }

  $onChanges(change) {
    if (change.categories) {
      this.categories = change.categories.currentValue.map((category) => {
        return Object.assign({}, category, {type: 'category'});
      });
    }
  }
}

export default OperationsCatalogueController;
