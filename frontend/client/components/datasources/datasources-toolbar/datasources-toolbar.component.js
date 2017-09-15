'use strict';

// Assets
import './datasources-toolbar.less';
import './modals/datasources-modals.less';
import './modals/modal-footer/modal-footer.less';
import './modals/external-file-modal/external-file-modal.less';
import './modals/hdfs-modal/hdfs-modal.less';
import './modals/library-modal/library-modal.less';
import templateUrl from './datasources-toolbar.html';

// App
import {datasourceModalMode} from 'COMMON/datasources/datasource-modal-mode.js';
import {datasourceContext} from 'APP/enums/datasources-context.js';


const DatasourcesToolbarComponent = {
  templateUrl,

  bindings: {
    context: '<'
  },

  controller: class DatasourcesToolbarController {
    constructor(
      $scope,
      DatasourcesModalsService,
      LibraryService
    ) {
      'ngInject';

      this.DatasourcesModalsService = DatasourcesModalsService;
      this.datasourceContext = datasourceContext;

      $scope.$watch(LibraryService.isUploadingInProgress, (newValue) => {
        this.uploadingInProgress = newValue;
      }, true);
    }


    addDatasource(datasourceType) {
      this.DatasourcesModalsService.openModal(datasourceType, datasourceModalMode.ADD);
    }
  }

};

export default DatasourcesToolbarComponent;
