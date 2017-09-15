'use strict';

import templateUrl from './datasources-element.html';
import './datasources-element.less';

const DatasourcesElementComponent = {
  templateUrl,
  bindings: {
    element: '<'
  },
  controller: class DatasourcesElementController {
    constructor() {
      'ngInject';

      this.visibilityLabel = {
        publicVisibility: 'Public',
        privateVisibility: 'Private'
      };

      this.typeIcon = {
        externalFile: 'sa-external-file',
        libraryFile: 'sa-library',
        hdfs: 'sa-hdfs',
        jdbc: 'sa-database',
        googleSpreadsheet: 'sa-google-spreadsheet'
      };
    }
  }
};

export default DatasourcesElementComponent;
