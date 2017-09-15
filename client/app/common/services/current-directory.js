'use strict';


export default class CurrentDirectory {
  constructor() {
    'ngInject';

    this.directory = null;
    this.uri = null;
    this.filter = '';
    this.items = [];
  }


  changeTo(directory) {
    this.directory = directory;
    // populate directory data
    this.name = this.directory.name;
    this.parents = this.directory.parents;
    this.path = this.directory.path;
    this.uri = this.directory.uri;
    this.updateItems();
  }


  isFiltered() {
    return !!this.filter;
  }


  setFilter(filter) {
    const newFilter = filter.trim();
    const filterHasChanged = this.filter !== newFilter;

    this.filter = newFilter;
    if (filterHasChanged) {
      this.updateItems();
    }
  }


  updateItems() {
    if (!this.filter) {
      this.items = [...this.directory.items];
      return;
    }

    const results = [];
    const filter = this.filter;
    const directoriesToSearch = [this.directory];

    while (directoriesToSearch.length) {
      const searchDirectory = directoriesToSearch.shift();

      searchDirectory.items.forEach((item) => {
        if (item.name.includes(filter)) {
          results.push(item);
        }
        if (item.isDirectory()) {
          directoriesToSearch.push(item);
        }
      });
    }

    this.items = results;
  }
}
