/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

import angular from 'angular';

import OperationsCatalogueModule from '../operations-catalogue/operations-catalogue.module.js';

import CanvasToolbarComponent from './canvas-toolbar/canvas-toolbar.component.js';
import CanvasComponent from './core-canvas/canvas.component.js';
import NewNodeComponent from './new-node/new-node.component.js';
import EditorComponent from './editor.component.js';
import GraphNodeComponent from './core-canvas/graph-node/graph-node.component.js';
import StatusIconComponent from './core-canvas/graph-node/status-icon/status-icon.component.js';
import PortStatusTooltipComponent from './port-status-tooltip/port-status-tooltip.component.js';
import CreateNodeInvitationComponent from './create-node-invitation/create-node-invitation.component.js';
import AdapterService from './core-canvas/adapter.service.js';
import CanvasService from './core-canvas/canvas.service.js';
import GraphStyleService from './core-canvas/graph-node/graph-style.service.js';

const appModule = angular
  .module('editor', [
    OperationsCatalogueModule
  ])
  .service('CanvasService', CanvasService)
  .service('AdapterService', AdapterService)
  .service('GraphStyleService', GraphStyleService)
  .component('newNode', NewNodeComponent)
  .component('canvasToolbar', CanvasToolbarComponent)
  .component('coreCanvas', CanvasComponent)
  .component('editor', EditorComponent)
  .component('graphNode', GraphNodeComponent)
  .component('statusIcon', StatusIconComponent)
  .component('portStatusTooltip', PortStatusTooltipComponent)
  .component('createNodeInvitation', CreateNodeInvitationComponent)
  .name;

export default appModule;
