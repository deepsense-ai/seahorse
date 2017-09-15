'use strict';

// Libs
import angular from 'angular';

// App
import PresetsApiService from './presets-api.service';
import SchedulingManagerApiService from './scheduling-manager-api.service';


export default angular
  .module('common.api', [])
  .service('PresetsApiService', PresetsApiService)
  .service('SchedulingManagerApiService', SchedulingManagerApiService)
  .name;
