'use strict';

// Libs
import angular from 'angular';

// App
import { NewScheduleModule } from './new-schedule/new-schedule.module';
import { ScheduleModule } from './schedule/schedule.module';
import { WorkflowSchedulesModule } from './workflow-schedules/workflow-schedules.module';


export const SchedulesModule = angular
  .module('schedules', [
    NewScheduleModule,
    ScheduleModule,
    WorkflowSchedulesModule
  ])
  .name;
