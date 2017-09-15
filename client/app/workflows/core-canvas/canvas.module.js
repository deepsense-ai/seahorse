'use strict';

import angular from 'angular';
import CanvasToolbarComponent from './canvas-toolbar/canvas-toolbar.component.js';
import CanvasComponent from './canvas.component.js';
import NewNodeService from './new-node/new-node.service.js';
import AdapterService from './canvas.adapter.service.js';
import CanvasService from './canvas.service.js';

const appModule = angular
  .module('core-canvas', [])
  .service('CanvasService', CanvasService)
  .service('AdapterService', AdapterService)
  .service('NewNodeService', NewNodeService)
  .component('canvasToolbar', CanvasToolbarComponent)
  .component('coreCanvas', CanvasComponent)
  .name;

export default appModule;
