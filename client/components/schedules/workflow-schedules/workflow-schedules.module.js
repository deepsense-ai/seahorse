'use strict';

// Libs
import angular from 'angular';

// App
import { WorkflowSchedulesComponent } from './workflow-schedules.component';


export const WorkflowSchedulesModule = angular
  .module('WorkflowSchedules', [])
  .component('workflowSchedules', WorkflowSchedulesComponent)
  .name;
