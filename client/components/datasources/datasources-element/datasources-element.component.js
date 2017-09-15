'use strict';

// Assets
import templateUrl from './datasources-element.html';
import './datasources-element.less';

// App
import {datasourceContext} from 'APP/enums/datasources-context.js';


const COOKIE_NAME = 'DELETE_DATASOURCE_COOKIE';

const DatasourcesElementComponent = {
  templateUrl,
  bindings: {
    element: '<',
    context: '<',
    onSelect: '&'
  },
  controller: class DatasourcesElementController {
    constructor(DeleteModalService, DatasourcesModalsService, datasourcesService) {
      'ngInject';

      this.DeleteModalService = DeleteModalService;
      this.DatasourcesModalsService = DatasourcesModalsService;
      this.datasourcesService = datasourcesService;

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

      this.context = this.context || datasourceContext.BROWSE_DATASOURCE;
    }

    selectDatasource(datasource) {
      if (this.isSelectable(datasource)) {
        this.onSelect({datasource});
      }
    }

    openDatasource(datasource, mode) {
      const type = datasource.params.datasourceType;
      this.DatasourcesModalsService.openModal(type, datasource, mode);
    }

    deleteDatasource(datasource) {
      this.DeleteModalService.handleDelete(() => {
        this.datasourcesService.deleteDatasource(datasource.id);
      }, COOKIE_NAME);
    }

    isSelectable(datasource) {
      if (this.context === datasourceContext.WRITE_DATASOURCE) {
        return datasource.params.datasourceType !== 'externalFile';
      }
      return this.context !== datasourceContext.BROWSE_DATASOURCE;
    }

    isOwner() {
      return this.datasourcesService.isCurrentUserOwnerOfDatasource(this.element);
    }
  }
};

export default DatasourcesElementComponent;
