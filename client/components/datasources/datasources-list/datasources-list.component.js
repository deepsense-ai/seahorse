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
    }

    sort(type, order) {

    }
  }
};

export default DatasourcesListComponent;
