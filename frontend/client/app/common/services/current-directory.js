/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


  containsDirectory(directoryName) {
    return this.items.filter((item) => {
        return item.kind === 'directory' && item.name === directoryName;
      }).length > 0;
  }
}
