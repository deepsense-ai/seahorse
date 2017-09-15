'use strict';

// Libs
import angular from 'angular';

// App
import DatasourcesService from './datasources.service';

export default angular
  .module('common.datasources', [])
  .service('datasourcesService', DatasourcesService)
  .name;
