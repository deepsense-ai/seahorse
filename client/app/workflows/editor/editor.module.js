'use strict';

import angular from 'angular';
import CanvasToolbarComponent from './canvas-toolbar/canvas-toolbar.component.js';
import CanvasComponent from './core-canvas/canvas.component.js';
import EditorComponent from './editor.component.js';
import NewNodeService from './new-node.service.js';
import AdapterService from './core-canvas/canvas.adapter.service.js';
import CanvasService from './core-canvas/canvas.service.js';

const appModule = angular
  .module('editor', [])
  .service('CanvasService', CanvasService)
  .service('AdapterService', AdapterService)
  .service('NewNodeService', NewNodeService)
  .component('canvasToolbar', CanvasToolbarComponent)
  .component('coreCanvas', CanvasComponent)
  .component('editor', EditorComponent)
  .name;

export default appModule;
