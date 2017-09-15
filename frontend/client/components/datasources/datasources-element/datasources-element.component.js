'use strict';

// Assets
import templateUrl from './datasources-element.html';
import './datasources-element.less';

// App
import {datasourceModalMode} from 'COMMON/datasources/datasource-modal-mode.js';
import {datasourceContext} from 'APP/enums/datasources-context.js';


const COOKIE_NAME = 'DELETE_DATASOURCE_COOKIE';
const DATASOURCE_ICON = {
  externalFile: 'sa-external-file',
  libraryFile: 'sa-library',
  hdfs: 'sa-hdfs',
  jdbc: 'sa-database',
  googleSpreadsheet: 'sa-google-spreadsheet'
};


const DatasourcesElementComponent = {
  templateUrl,

  bindings: {
    context: '<',
    datasource: '<',
    onSelect: '&'
  },

  controller: class DatasourcesElementController {
    constructor(
      DeleteModalService,
      DatasourcesModalsService,
      datasourcesService
    ) {
      'ngInject';

      this.DeleteModalService = DeleteModalService;
      this.DatasourcesModalsService = DatasourcesModalsService;
      this.datasourcesService = datasourcesService;

      this.visibilityLabel = {
        publicVisibility: 'Public',
        privateVisibility: 'Private'
      };
    }


    $onInit() {
      this.datasourceIcon = DATASOURCE_ICON[this.datasource.params.datasourceType];
      this.context = this.context || datasourceContext.BROWSE_DATASOURCE;

      if (this.isOwner()) {
        this.ownerName = 'You';
        this.openActionIcon = 'sa-edit';
        this.openActionTitle = 'Edit';
      } else {
        this.ownerName = this.datasource.ownerName;
        this.openActionIcon = 'sa-view';
        this.openActionTitle = 'See';
      }
    }


    deleteDatasource() {
      this.DeleteModalService.handleDelete(() => {
        this.datasourcesService.deleteDatasource(this.datasource.id);
      }, COOKIE_NAME);
    }


    isOwner() {
      return this.datasourcesService.isCurrentUserOwnerOfDatasource(this.datasource);
    }


    isSelectable() {
      if (this.context === datasourceContext.WRITE_DATASOURCE) {
        return this.datasource.params.datasourceType !== 'externalFile';
      }
      return this.context !== datasourceContext.BROWSE_DATASOURCE;
    }


    openDatasource() {
      const datasourceType = this.datasource.params.datasourceType;
      const mode = this.isOwner() ? datasourceModalMode.EDIT : datasourceModalMode.VIEW;

      this.DatasourcesModalsService.openModal(datasourceType, mode, this.datasource);
    }


    selectDatasource() {
      if (this.isSelectable()) {
        this.onSelect({ datasource: this.datasource });
      }
    }
  }
};

export default DatasourcesElementComponent;
