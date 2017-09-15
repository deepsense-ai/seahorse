'use strict';

// Libs
import angular from 'angular';

// App
import { NewScheduleComponent } from './new-schedule.component';


export const NewScheduleModule = angular
  .module('newSchedule', [])
  .component('newSchedule', NewScheduleComponent)
  .name;
