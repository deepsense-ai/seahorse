//TODO Move it to the nodeType table and get from there
const NODE_WIDTH = 160;
const NODE_HEIGHT = 60;

const OFFSET = 30;

class CanvasService {
  /*@ngInject*/
  constructor(AdapterService) {
    this.AdapterService = AdapterService;

    //sliding window size
    this.windowSize = {
      width: 0,
      height: 0,
    };

    //sliding window position
    this.windowPosition = {
      x: 0,
      y: 0,
    };

    this.scale = 1;

    //TODO to remove
    this.zoomStep = 0.1;
    this.moveStep = 50;
  }

  initialize(jsPlumbContainer, slidingWindow) {
    this.$slidingWindow = $(slidingWindow);
    this.AdapterService.initialize(jsPlumbContainer);

    this.windowSize = this.getWindowSize();
    this.setZoom(1);
    this.applyToWindow();
  }

  getWindowSize() {
    return {
      width: this.$slidingWindow.width(),
      height: this.$slidingWindow.height()
    }
  }

  applyToWindow() {
    this.$slidingWindow.css({
      'transform': `translate(${this.windowPosition.x}px, ${this.windowPosition.y}px) scale(${this.scale})`
    });
  }

  setZoom(zoom) {
    this.scale = zoom;
    this.AdapterService.setZoom(this.scale);
  }

  setPosition(position) {
    this.windowPosition = position;
    this.applyToWindow();
  }

  setCollection(items) {
    this.collection = items;
    this.AdapterService.setItems(items);
  }

  setEdges(edges) {
    this.AdapterService.setEdges(edges);
  }

  render() {
    this.AdapterService.render();
    this.fit();
  }

  moveWindow(x = 0, y = 0) {
    this.setPosition({
      x: this.windowPosition.x + x,
      y: this.windowPosition.y + y
    });
  }

  fit() {
    this.windowSize = this.getWindowSize();
    const boundaries = {
      left: Infinity,
      top: 0,
      right: 0,
      bottom: Infinity
    };

    Object.keys(this.collection).forEach((key) => {
      boundaries.left = Math.min(boundaries.left, this.collection[key].x);
      boundaries.right = Math.max(boundaries.right, this.collection[key].x + NODE_WIDTH);
      boundaries.top = Math.max(boundaries.top, this.collection[key].y + NODE_HEIGHT);
      boundaries.bottom = Math.min(boundaries.bottom, this.collection[key].y);
    });

    this.setZoom(Math.min(
      (this.windowSize.width - OFFSET) / (boundaries.right - boundaries.left),
      (this.windowSize.height - OFFSET) / (boundaries.top - boundaries.bottom)
    ));

    this.setPosition({
      x: -this.scale * ((boundaries.left + boundaries.right) / 2) + (this.windowSize.width) / 2,
      y: -this.scale * ((boundaries.top + boundaries.bottom) / 2) + (this.windowSize.height) / 2
    })
  }

  centerZoom(zoom) {
    const ratio =  zoom / this.scale;

    this.setZoom(zoom);
    this.setPosition({
      x: this.windowSize.width/2 -(this.windowSize.width/2 - this.windowPosition.x) * ratio,
      y: this.windowSize.height/2- (this.windowSize.height/2 - this.windowPosition.y) * ratio
    })
  }

  //TODO to remove
  zoomIn() {
    this.centerZoom(this.scale + this.zoomStep);
  }

  //TODO to remove
  zoomOut() {
    this.centerZoom(this.scale - this.zoomStep);
  }
}

export default CanvasService;
