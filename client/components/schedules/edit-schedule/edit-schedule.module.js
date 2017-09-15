'use strict';

// Libs
import angular from 'angular';

// App
import { EditScheduleComponent } from './edit-schedule.component';


export const EditScheduleModule = angular
  .module('editSchedule', [])
  .component('editSchedule', EditScheduleComponent)
  .name;
