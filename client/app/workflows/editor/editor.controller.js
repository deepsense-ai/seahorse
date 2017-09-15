'use strict';
const ZOOM_STEP = 0.1;

class EditorController {
  constructor(CanvasService, UUIDGenerator, Operations, MouseEvent,  $element) {
    'ngInject';
    this.CanvasService = CanvasService;
    this.UUIDGenerator = UUIDGenerator; //TODO remove when services are refactored
    this.Operations = Operations;       //TODO remove when services are refactored
    this.MouseEvent = MouseEvent;
    this.$element = $element;
  }

  $postLink() {
    this.bindEvents();
  }

  $onDestroy() {
    this.$canvas.off();
  }

  bindEvents() {
    this.$canvas = $(this.$element[0].querySelector('core-canvas'));
    this.$toolbar = $(this.$element[0].querySelector('canvas-toolbar'));
    //Wheel handling
    this.$canvas.bind('wheel', (e) => {
      let zoomDelta = ZOOM_STEP;
      if (e.originalEvent.deltaY < 0) {
        zoomDelta = -1 * ZOOM_STEP;
      }
      this.CanvasService.centerZoom(zoomDelta);
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
        this.newNodeData = {
          x: originalEvent.offsetX,
          y: originalEvent.offsetY
        };
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
      this.newNodeData = {
        x: originalEvent.offsetX,
        y: originalEvent.offsetY
      };
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
    const [x, y] = this.CanvasService.translatePosition(100, 100);
    this.newNodeData = {
      x: x,
      y: y
    }
  }

  onConnectionAbort(newNodeData) {
    this.newNodeData = newNodeData;
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
    if (this.newNodeData.source) {
      const newEdge = this.workflow.createEdge({
        from: {
          nodeId: this.newNodeData.source,
          portIndex: 0
        },
        to: {
          nodeId: node.id,
          portIndex: 0
        }
      });
      this.workflow.addEdge(newEdge);
    }
    this.newNodeData = null;
  }
}

export default EditorController;
