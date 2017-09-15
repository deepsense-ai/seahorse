'use strict';

// Libs
import angular from 'angular';

// App
import DatasourcesPanelComponent from './datasources-panel/datasources-panel.component.js';
import DatasourcesListComponent from './datasources-list/datasources-list.component.js';
import DatasourcesPanelService from './datasources-panel/datasources-panel.service.js';

export const datasourcesModule = angular
  .module('datasources', [])
  .component('datasourcesPanel', DatasourcesPanelComponent)
  .component('datasourcesList', DatasourcesListComponent)
  .service('DatasourcesPanelService', DatasourcesPanelService)
  .name;
