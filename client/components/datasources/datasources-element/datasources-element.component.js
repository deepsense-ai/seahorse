'use strict';

import templateUrl from './datasources-element.html';
import './datasources-element.less';

const DatasourcesElementComponent = {
  templateUrl,
  bindings: {
    element: '<',
    context: '<',
    onSelect: '&'
  },
  controller: class DatasourcesElementController {
    constructor(UserService) {
      'ngInject';

      this.UserService = UserService;

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

      this.context = this.context || 'read-only';
    }

    edit(datasource) {
      console.warn(datasource); //eslint-disable-line
    }

    delete(datasource) {
      console.warn(datasource); //eslint-disable-line
    }

    isSelectable() {
      return this.context !== 'read-only';
    }

    isOwner() {
      return this.UserService.getSeahorseUser().id === this.element.ownerId;
    }
  }
};

export default DatasourcesElementComponent;
