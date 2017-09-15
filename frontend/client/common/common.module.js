'use strict';

// Libs
import angular from 'angular';

// App
import apiModule from './api/api.module';
import datasourcesModule from './datasources/datasources.module';
import directivesModule from './directives/directives.module';
import filtersModule from './filters/filters.module';
import helpersModule from './helpers/helpers.module';
import schedulesModule from './schedules/schedules.module';


export const CommonModule = angular
  .module('common', [
    apiModule,
    datasourcesModule,
    directivesModule,
    filtersModule,
    helpersModule,
    schedulesModule
  ])
  .name;
