'use strict';

// Libs
import angular from 'angular';

// App
import { datasourcesModule } from './datasources/datasources.module';
import { SchedulesModule } from './schedules/schedules.module';


export const ComponentModule = angular
  .module('components', [
    datasourcesModule,
    SchedulesModule
  ])
  .name;
