'use strict';

import angular from 'angular';
import CanvasToolbarComponent from './canvas-toolbar/canvas-toolbar.component.js';
import CanvasComponent from './canvas.component.js';
import AdapterService from './canvas.adapter.service.js';
import CanvasService from './canvas.service.js';

const appModule = angular
  .module('core-canvas', [])
  .service('CanvasService', CanvasService)
  .service('AdapterService', AdapterService)
  .component('canvasToolbar', CanvasToolbarComponent)
  .component('coreCanvas', CanvasComponent)
  .name;

export default appModule;
