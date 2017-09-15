'use strict';

import DatasourcesToolbarTemplate from './datasources-toolbar.html';

// Assets
import './datasources-toolbar.less';
import './modals/datasources-modals.less';
import './modals/modal-footer/modal-footer.less';
import './modals/external-file-modal/external-file-modal.less';
import './modals/database-modal/database-modal.less';
import './modals/google-spreadsheet-modal/google-spreadsheet-modal.less';
import './modals/hdfs-modal/hdfs-modal.less';
import './modals/library-modal/library-modal.less';

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
