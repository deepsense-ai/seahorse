'use strict';

import templateUrl from './datasources-list.html';
import './datasources-list.less';

const DatasourcesListComponent = {
  templateUrl,
  bindings: {
    datasources: '<',
    context: '<',
    onSelect: '&'
  },
  controller: class DatasourcesListController {
    constructor() {
      'ngInject';

      this.sort('creationDateTime', true);
    }

    sort(type, order) {
      this.filterType = type;
      this.filterOrder = order;
    }
  }
};

export default DatasourcesListComponent;
