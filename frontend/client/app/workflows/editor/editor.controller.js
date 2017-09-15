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

const NEW_NODE_ELEMENT_WIDTH = 200;
const ZOOM_STEP = 0.05;
const TRANSITION_TIME = 0.5;

class EditorController {
  constructor($rootScope, $scope, $element, CanvasService, AdapterService, UUIDGenerator, Operations, OperationsHierarchyService, MouseEvent) {
    'ngInject';

    this.CanvasService = CanvasService;
    this.AdapterService = AdapterService;
    this.UUIDGenerator = UUIDGenerator; //TODO remove when services are refactored
    this.Operations = Operations;       //TODO remove when services are refactored
    this.OperationsHierarchyService = OperationsHierarchyService;       //TODO remove when services are refactored
    this.MouseEvent = MouseEvent;
    this.$element = $element;
    this.$rootScope = $rootScope;

    this.categories = Operations.getCatalog();

    $scope.$watch(this.workflow.getNodes, (newValue) => {
      this.canShowInvitationToCreate = Object.keys(newValue).length === 0;
    }, true);

    this.removeListener = $rootScope.$on('Keyboard.KEY_PRESSED_DEL', () => {
      this.hideTooltip();
    });
  }

  $postLink() {
    this.bindEvents();
    this.containment = $(this.$element[0].querySelector('.editor'));

    this.AdapterService.setMouseOverOnPortFunction(this.showTooltip.bind(this));
    this.AdapterService.setMouseOutOnPortFunction(this.hideTooltip.bind(this));
  }

  $onDestroy() {
    this.$canvas.off();
    this.$element.off();
    this.removeListener();
  }

  bindEvents() {
    this.$canvas = $(this.$element[0].querySelector('core-canvas'));
    this.$toolbar = $(this.$element[0].querySelector('canvas-toolbar'));
    //Wheel handling
    this.$canvas.bind('wheel', ($event) => {
      const cursorX = $event.originalEvent.clientX - this.$element[0].getBoundingClientRect().left;
      const cursorY = $event.originalEvent.clientY - this.$element[0].getBoundingClientRect().top;
      // Normalize zoom delta, so FF scrolling seems similar in speed compared to Chrome
      const FF_ZOOM_RATIO = 17;
      const normalizedZoom = (!$event.originalEvent.wheelDelta) ? $event.originalEvent.deltaY * FF_ZOOM_RATIO : $event.originalEvent.deltaY;
      const zoomDelta = normalizedZoom / 1000;
      this.CanvasService.zoomToPosition(zoomDelta, cursorX, cursorY);
    });

    // Drag handling in JSPlumb
    const moveHandler = ($event) => {
      if (this.MouseEvent.isModKeyDown($event)) {
        this.CanvasService.moveWindow($event.originalEvent.movementX, $event.originalEvent.movementY);
      } else {
        this.$canvas.off('mousemove', moveHandler);
      }
    };

    this.$canvas.bind('mousedown', ($event) => {
      if (this.MouseEvent.isModKeyDown($event)) {
        this.$canvas.bind('mousemove', moveHandler);
      }
    });

    this.$canvas.bind('mouseup', () => {
      this.$canvas.off('mousemove', moveHandler);
    });

    // Drag and Drop from toolbar handling
    this.$canvas.bind('drop', ($event) => {
      const originalEvent = $event.originalEvent;
      if (originalEvent.dataTransfer.getData('draggableExactType') === 'graphNode') {
        this.startWizardFromEvent($event);
      }
    });

    this.$element.bind('mousedown', () => {
      if (this.newNodeData) {
        this.newNodeData = null;
      }
    });

    this.$toolbar.bind('mousedown', ($event) => {
      $event.stopPropagation();
    });

    this.$canvas.bind('contextmenu', ($event) => {
      this.startWizardFromEvent($event);
      return false;
    });

    /**
     * Needed to manually restore focus to Canvas after clicking on elements inside editor.
     * It is used by cloning service.
     */
    this.$canvas.bind('click', () => {
      this.$canvas[0].querySelector('.canvas').focus();
    });
  }

  zoomIn() {
    this.CanvasService.centerZoom(ZOOM_STEP);
  }

  zoomOut() {
    this.CanvasService.centerZoom(-1 * ZOOM_STEP);
  }

  fit() {
    this.CanvasService.fit();
  }

  fullScreen() {
    //TODO: add after the design of scene changes
  }

  newNode($event) {
    const [x, y] = this.CanvasService.translateScreenToCanvasPosition(100, 100);
    this.startWizard(x, y);
  }

  onConnectionAbort(newNodeData) {
    const portPositionX = newNodeData.x - (NEW_NODE_ELEMENT_WIDTH / 2 * this.CanvasService.scale);
    const portPositionY = newNodeData.y - this.$element[0].getBoundingClientRect().top;
    const [x, y] = this.CanvasService.translateScreenToCanvasPosition(portPositionX, portPositionY);
    this.startWizard(x, y, newNodeData.endpoint);
  }

  startWizardFromEvent($event) {
    const positionX = $event.clientX;
    const positionY = $event.clientY - this.$element[0].getBoundingClientRect().top;
    const [x, y] = this.CanvasService.translateScreenToCanvasPosition(positionX, positionY);
    this.startWizard(x, y);
  }

  startWizard(x, y, endpoint = null) {
    this.newNodeData = null;
    this.$rootScope.$digest();
    if (this.isEditable) {
      const newNodeData = {
        x: x,
        y: y,
        nodeId: null,
        portIndex: null,
        typeQualifier: null
      };

      // After some time endpoint paramters are wiped by jsPlumb,
      // and we need those params to render the temporary edges or after wizard the final edges.
      if (endpoint) {
        newNodeData.nodeId = endpoint.getParameter('nodeId');
        newNodeData.portIndex = endpoint.getParameter('portIndex');
        newNodeData.typeQualifier = this.workflow.getNodeById(newNodeData.nodeId).output[newNodeData.portIndex].typeQualifier;
        const catalog = this.Operations.getCatalog();
        const filterForCatalog = this.Operations.getFilterForTypeQualifier(newNodeData.typeQualifier[0]);
        this.categories = this.Operations.filterCatalog(catalog, filterForCatalog);
      } else {
        this.categories = this.Operations.getCatalog();
      }

      this.newNodeData = newNodeData;
    }
  }

  onDisplayRectChange(rect) {
    let delta;
    const {left, right, bottom} = this.containment[0].getBoundingClientRect();
    delta = Math.min(right - rect.right, 0);
    if (delta === 0) {
      delta = Math.max(left - rect.left, 0);
    }
    this.CanvasService.moveWindow(
      delta,
      Math.min(bottom - rect.bottom, 0),
      TRANSITION_TIME);
  }

  onSelect(operationId) {
    const newNodeElement = this.$element[0].querySelector('new-node');
    //TODO move it out of here to some service when they're refactored
    const params = {
      id: this.UUIDGenerator.generateUUID(),
      x: newNodeElement.offsetLeft,
      y: newNodeElement.offsetTop,
      operation: this.Operations.get(operationId)
    };
    const node = this.workflow.createNode(params);
    this.workflow.addNode(node);
    if (this.newNodeData.nodeId) {
      const newEdge = this.workflow.createEdge({
        from: {
          nodeId: this.newNodeData.nodeId,
          portIndex: this.newNodeData.portIndex
        },
        to: {
          nodeId: node.id,
          portIndex: this.getMatchingPortIndex(node, this.newNodeData.typeQualifier)
        }
      });
      this.workflow.addEdge(newEdge);
    }
    this.newNodeData = null;
  }

  //TODO move to service that is aware of OperationsHierarchy Service and GraphNode
  getMatchingPortIndex(node, typeQualifier) {
    let portIndex = 0;
    node.input.forEach((port, index) => {
      if (this.OperationsHierarchyService.IsDescendantOf(typeQualifier, port.typeQualifier)) {
        portIndex = index;
      }
    });
    return portIndex;
  }

  showTooltip(portEl, portObject) {
    this.portElement = portEl;
    this.portObject = portObject;
    this.isTooltipVisible = true;

    [this.tooltipX, this.tooltipY] = this.getPosition();
  }

  hideTooltip() {
    this.portElement = null;
    this.portObject = null;
    this.isTooltipVisible = false;
  }

  getPosition() {
    // To show tooltip in the same position for both inputs and outputs
    const adjustment = (this.portObject.type === 'input') ? -5 : 5;

    return [
      Math.round(this.portElement.getBoundingClientRect().right),
      Math.round(this.portElement.getBoundingClientRect().top - this.$canvas[0].getBoundingClientRect().top + adjustment)
    ];
  }
}

export default EditorController;
