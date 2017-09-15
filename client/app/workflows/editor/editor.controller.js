'use strict';

const NEW_NODE_ELEMENT_WIDTH = 240;
const ZOOM_STEP = 0.1;
const TRANSITION_TIME = 0.5;

class EditorController {
  constructor($rootScope, CanvasService, AdapterService, UUIDGenerator, Operations, OperationsHierarchyService, MouseEvent,  $element) {
    'ngInject';
    this.CanvasService = CanvasService;
    this.AdapterService = AdapterService;
    this.UUIDGenerator = UUIDGenerator; //TODO remove when services are refactored
    this.Operations = Operations;       //TODO remove when services are refactored
    this.OperationsHierarchyService = OperationsHierarchyService;       //TODO remove when services are refactored
    this.MouseEvent = MouseEvent;
    this.$element = $element;

    this.categories = Operations.getCatalog();

    $rootScope.$on('Keyboard.KEY_PRESSED_DEL', () => {
      this.hideTooltip();
    });
  }

  $postLink() {
    this.bindEvents();
    this.containment = $(this.$element[0].querySelector('.flowchart-box'));

    this.AdapterService.handleShowTooltip(this.showTooltip.bind(this));
    this.AdapterService.handleHideTooltip(this.hideTooltip.bind(this));
  }

  $onDestroy() {
    this.$canvas.off();
  }

  bindEvents() {
    this.$canvas = $(this.$element[0].querySelector('core-canvas'));
    this.$toolbar = $(this.$element[0].querySelector('canvas-toolbar'));
    //Wheel handling
    this.$canvas.bind('wheel', (e) => {
      const cursorX = e.originalEvent.clientX - this.$element[0].getBoundingClientRect().left;
      const cursorY = e.originalEvent.clientY - this.$element[0].getBoundingClientRect().top;
      let zoomDelta = ZOOM_STEP;
      if (e.originalEvent.deltaY < 0) {
        zoomDelta = -1 * ZOOM_STEP;
      }
      this.CanvasService.zoomToPosition(zoomDelta, cursorX, cursorY);
    });

    // Drag handling in JSPlumb
    const moveHandler = (event) => {
      if (this.MouseEvent.isModKeyDown(event)) {
        this.CanvasService.moveWindow(event.originalEvent.movementX, event.originalEvent.movementY);
      } else {
        this.$canvas.off('mousemove', moveHandler);
      }
    };

    this.$canvas.bind('mousedown', () => {
      if (this.MouseEvent.isModKeyDown(event)) {
        this.$canvas.bind('mousemove', moveHandler);
      }
    });

    this.$canvas.bind('mouseup', () => {
      this.$canvas.off('mousemove', moveHandler);
    });

    // Drag and Drop from toolbar handling
    this.$canvas.bind('drop', (event) => {
      const originalEvent = event.originalEvent;
      if (originalEvent.dataTransfer.getData('draggableExactType') === 'graphNode') {
        this.startWizard(originalEvent.offsetX, originalEvent.offsetY);
      }
    });

    this.$element.bind('mousedown', () => {
      if (this.newNodeData) {
        this.newNodeData = null;
      }
    });

    this.$toolbar.bind('mousedown', (event) => {
      event.stopPropagation();
    });

    // Drag and Drop from toolbar handling
    this.$canvas.bind('contextmenu', (event) => {
      const originalEvent = event.originalEvent;
      this.startWizard(originalEvent.offsetX, originalEvent.offsetY);
      return false;
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
    const portPositionY = newNodeData.y - this.$canvas.offset().top;
    const [x, y] = this.CanvasService.translateScreenToCanvasPosition(portPositionX , portPositionY);
    this.startWizard(x, y, newNodeData.endpoint);
  }

  startWizard(x, y, endpoint = null) {
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
        this.categories = this.Operations.filterCatalog(this.Operations.getCatalog(), this.getFilterForCatalog(newNodeData.typeQualifier));
      } else {
        this.categories = this.Operations.getCatalog();
      }

      this.newNodeData = newNodeData;
    }
  }

  onDisplayRectChange(rect) {
    const {left, right, bottom} = this.containment[0].getBoundingClientRect();
    const rightDelta = Math.min(right - rect.right, 0);
    const leftDelta = Math.max(left - rect.left, rightDelta);
    this.CanvasService.moveWindow(
      leftDelta,
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

  //TODO move to service that is aware of OperationsHierarchy Service and Operation and GraphNode??
  getFilterForCatalog(inputQualifier) {
    return (item) => {
      return this.Operations.get(item.id).ports.input.filter((port) =>
        this.OperationsHierarchyService.IsDescendantOf(inputQualifier, port.typeQualifier)
      ).length;
    };
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
