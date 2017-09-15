'use strict';

// Libs
import angular from 'angular';

// App
import { EditScheduleModule } from './edit-schedule/edit-schedule.module';
import { ScheduleModule } from './schedule/schedule.module';
import { WorkflowSchedulesModule } from './workflow-schedules/workflow-schedules.module';


export const SchedulesModule = angular
  .module('schedules', [
    EditScheduleModule,
    ScheduleModule,
    WorkflowSchedulesModule
  ])
  .name;
