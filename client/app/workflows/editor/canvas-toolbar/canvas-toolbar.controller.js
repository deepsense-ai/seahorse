'use strict';

class CanvasToolbarController {
  zoomIn() {
    this.onZoomIn();
  }

  zoomOut() {
    this.onZoomOut();
  }

  fullScreen() {
    //TODO
  }

  fit() {
    this.onFit();
  }

  newNode() {
    this.onNewNode();
  }
}

export default CanvasToolbarController;
