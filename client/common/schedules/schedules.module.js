'use strict';

// Libs
import angular from 'angular';

// App
import WorkflowSchedulesService from './workflow-schedules.service';

export default angular
  .module('common.schedules', [])
  .service('WorkflowSchedulesService', WorkflowSchedulesService)
  .name;
