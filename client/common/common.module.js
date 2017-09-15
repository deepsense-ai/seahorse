'use strict';

// Libs
import angular from 'angular';

// App
import commonApiModule from './api/api.module';
import commonDirectivesModule from './directives/directives.module';
import commonFiltersModule from './filters/filters.module';
import commonHelpersModule from './helpers/helpers.module';
import commonSchedulesModule from './schedules/schedules.module';


export const CommonModule = angular
  .module('common', [
    commonApiModule,
    commonDirectivesModule,
    commonFiltersModule,
    commonHelpersModule,
    commonSchedulesModule
  ])
  .name;
