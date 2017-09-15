'use strict';

// Libs
import angular from 'angular';

// App
import { SchedulesModule } from './schedules/schedules.module';


export const ComponentModule = angular
  .module('components', [
    SchedulesModule
  ])
  .name;
