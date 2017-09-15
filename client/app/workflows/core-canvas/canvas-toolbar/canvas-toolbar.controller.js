'use strict';

const ZOOM_STEP = 0.1;

class CanvasToolbarController {
  constructor(CanvasService, NewNodeService) {
    'ngInject';
    this.CanvasService = CanvasService;
    this.NewNodeService = NewNodeService;
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

export default CanvasToolbarController;
