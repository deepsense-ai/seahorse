'use strict';
const ZOOM_STEP = 0.1;

class EditorController {
  constructor(NewNodeService, CanvasService, MouseEvent,  $element) {
    'ngInject';
    this.NewNodeService = NewNodeService;
    this.CanvasService = CanvasService;
    this.MouseEvent = MouseEvent;
    this.$element = $element;
  }

  $postLink() {
    const canvasContainer = this.$element[0].querySelector('.flowchart-box');

    this.bindEvents();
    this.NewNodeService.initialize(canvasContainer);
  }

  $onDestroy() {
    this.$canvas.off();
  }

  bindEvents() {
    this.$canvas = $(this.$element[0].querySelector('core-canvas'));
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
        this.NewNodeService.startWizard(originalEvent.layerX, originalEvent.layerY);
      }
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

  newNode() {
    this.NewNodeService.startWizard(100, 100);
  }
}

export default EditorController;
