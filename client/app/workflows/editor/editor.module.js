'use strict';

import angular from 'angular';
import CanvasToolbarComponent from './canvas-toolbar/canvas-toolbar.component.js';
import CanvasComponent from './core-canvas/canvas.component.js';
import NewNodeComponent from './new-node/new-node.component.js';
import EditorComponent from './editor.component.js';
import GraphNodeComponent from './core-canvas/graph-node/graph-node.component.js';
import AdapterService from './core-canvas/canvas.adapter.service.js';
import CanvasService from './core-canvas/canvas.service.js';
import GraphStyleService from './core-canvas/graph-node/graph-style.service.js';

const appModule = angular
  .module('editor', [])
  .service('CanvasService', CanvasService)
  .service('AdapterService', AdapterService)
  .service('GraphStyleService', GraphStyleService)
  .component('newNode', NewNodeComponent)
  .component('canvasToolbar', CanvasToolbarComponent)
  .component('coreCanvas', CanvasComponent)
  .component('editor', EditorComponent)
  .component('graphNode', GraphNodeComponent)
  .name;

export default appModule;
