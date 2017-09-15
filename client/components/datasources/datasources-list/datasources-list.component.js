'use strict';

import templateUrl from './datasources-list.html';
import './datasources-list.less';

const DatasourcesListComponent = {
  templateUrl,
  bindings: {},
  controller: class DatasourcesListController {
    constructor() {
      'ngInject';
    }
  }
};

export default DatasourcesListComponent;
