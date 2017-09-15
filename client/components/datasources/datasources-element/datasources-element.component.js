'use strict';

import templateUrl from './datasources-element.html';
import './datasources-element.less';

const COOKIE_NAME = 'DELETE_DATASOURCE_COOKIE';

const DatasourcesElementComponent = {
  templateUrl,
  bindings: {
    element: '<',
    context: '<',
    onSelect: '&'
  },
  controller: class DatasourcesElementController {
    constructor(UserService, DeleteModalService, DatasourcesModalsService, datasourcesService) {
      'ngInject';

      _.assign(this, {UserService, DeleteModalService, DatasourcesModalsService, datasourcesService});

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

    editDatasource(datasource) {
      const type = datasource.params.datasourceType;
      this.DatasourcesModalsService.openModal(type, datasource);
    }

    deleteDatasource(datasource) {
      this.DeleteModalService.handleDelete(() => {
        this.datasourcesService.deleteDatasource(datasource.id);
      }, COOKIE_NAME);
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
