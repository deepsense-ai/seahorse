'use strict';

import angular from 'angular';
import CanvasComponent from './canvas.component.js';
import AdapterService from './canvas.adapter.service.js';
import CanvasService from './canvas.service.js';

const appModule = angular
  .module('deepsense-experimental', [])
  .service('CanvasService', CanvasService)
  .service('AdapterService', AdapterService)
  .component('coreCanvas', CanvasComponent)
  .name;

export default appModule;
