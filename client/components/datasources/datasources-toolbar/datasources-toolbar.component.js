'use strict';

import DatasourcesToolbarTemplate from './datasources-toolbar.html';
import './datasources-toolbar.less';
import './modals/datasources-modals.less';

const DatasourcesToolbarComponent = {
  bindings: {},
  templateUrl: DatasourcesToolbarTemplate,
  controller: class DatasourcesToolbarController {
    constructor(DatasourcesModalsService) {
      'ngInject';

      _.assign(this, {DatasourcesModalsService});
    }

    openModal(type) {
      this.DatasourcesModalsService.openModal(type);
    }
  }

};

export default DatasourcesToolbarComponent;
