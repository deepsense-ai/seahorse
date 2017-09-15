'use strict';

// Libs
import angular from 'angular';

// App
import { ScheduleComponent } from './schedule.component';


export const ScheduleModule = angular
  .module('schedule', [])
  .component('schedule', ScheduleComponent)
  .name;
