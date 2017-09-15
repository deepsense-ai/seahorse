'use strict';

// Libs
import angular from 'angular';

// App
import DatasourcesPanelComponent from './datasources-panel/datasources-panel.component.js';
import DatasourcesListComponent from './datasources-list/datasources-list.component.js';
import DatasourcesToolbarComponent from './datasources-toolbar/datasources-toolbar.component.js';
import FileSettingsComponent from './datasources-toolbar/file-settings/file-settings.component.js';
import DatasourcesPanelService from './datasources-panel/datasources-panel.service.js';
import DatasourcesModalsService from './datasources-toolbar/modals/datasources-modals.service.js';

// Modals controllers
import DatabaseModalController from './datasources-toolbar/modals/database-modal/database-modal.controller.js';
import ExternalFileModalController from './datasources-toolbar/modals/external-file-modal/external-file-modal.controller.js';
import GoogleSpreadsheetModalController from './datasources-toolbar/modals/google-spreadsheet-modal/google-spreadsheet-modal.controller.js';
import HdfsModalController from './datasources-toolbar/modals/hdfs-modal/hdfs-modal.controller.js';
import LibraryModalController from './datasources-toolbar/modals/library-modal/library-modal.controller.js';

export const datasourcesModule = angular
  .module('datasources', [])
  .component('datasourcesPanel', DatasourcesPanelComponent)
  .component('datasourcesList', DatasourcesListComponent)
  .component('datasourcesToolbar', DatasourcesToolbarComponent)
  .component('fileSettings', FileSettingsComponent)
  .service('DatasourcesPanelService', DatasourcesPanelService)
  .service('DatasourcesModalsService', DatasourcesModalsService)
  .controller('DatabaseModalController', DatabaseModalController)
  .controller('ExternalFileModalController', ExternalFileModalController)
  .controller('GoogleSpreadsheetModalController', GoogleSpreadsheetModalController)
  .controller('HdfsModalController', HdfsModalController)
  .controller('LibraryModalController', LibraryModalController)
  .name;
