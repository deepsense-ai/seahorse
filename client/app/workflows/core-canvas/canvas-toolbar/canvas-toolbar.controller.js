'use strict';

const ZOOM_STEP = 0.1;

class CanvasToolbarController {
  constructor(CanvasService) {
    'ngInject';
    this.CanvasService = CanvasService;
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
    //TODO
  }
}

export default CanvasToolbarController;
