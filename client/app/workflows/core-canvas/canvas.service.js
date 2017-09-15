import _ from 'lodash';


//TODO Move it to the nodeType table and get from there
const NODE_WIDTH = 160;
const NODE_HEIGHT = 60;

const OFFSET = 30;

const ZOOM_BOUNDS = [0.5, 1.5];
const ZOOM_STEP = 0.1;

class CanvasService {
  /*@ngInject*/
  constructor(AdapterService) {
    this.AdapterService = AdapterService;

    this.slidingWindowSize = {
      width: 0,
      height: 0,
    };

    this.slidingWindowPosition = {
      x: 0,
      y: 0,
    };

    this.scale = 1;

    //TODO remove
    this.moveStep = 50;
  }

  initialize(jsPlumbContainer, slidingWindow) {
    this.$slidingWindow = $(slidingWindow);
    this.AdapterService.initialize(jsPlumbContainer);
    this.slidingWindowSize = this.getWindowSize();
    this.setZoom(1);
    this.bindEvents();
    this.applyToWindow();
  }

  getWindowSize() {
    return {
      width: this.$slidingWindow.width(),
      height: this.$slidingWindow.height()
    }
  }

  bindEvents() {
    //Wheel handling
    this.$slidingWindow.bind('wheel', (e) => {
      let zoomDelta = ZOOM_STEP;
      if (e.originalEvent.deltaY < 0) {
        zoomDelta = -1 * ZOOM_STEP;
      }
      this.centerZoom(zoomDelta);
    });

    // Drag handling
    const moveHandler = (event) => {
      if (event.ctrlKey) {
        this.moveWindow(event.originalEvent.movementX, event.originalEvent.movementY);
      } else {
        this.$slidingWindow.off('mousemove', moveHandler);
      }
    };

    this.$slidingWindow.bind('mousedown', () => {
      if (event.ctrlKey) {
        this.$slidingWindow.bind('mousemove', moveHandler);
      }
    });

    this.$slidingWindow.bind('mouseup', () => {
      this.$slidingWindow.off('mousemove', moveHandler);
    });
  }

  applyToWindow() {
    this.$slidingWindow.css({
      'transform': `translate(${this.slidingWindowPosition.x}px, ${this.slidingWindowPosition.y}px) scale(${this.scale})`
    });
  }

  setZoom(zoom) {
    this.scale = _.clamp(zoom, ...ZOOM_BOUNDS);
    this.AdapterService.setZoom(this.scale);
  }

  setPosition(position) {
    this.slidingWindowPosition = position;
    this.applyToWindow();
  }

  setWorkflow(workflow) {
    this.nodes = workflow.getNodes();
    this.AdapterService.setWorkflow(workflow);
  }

  render() {
    this.AdapterService.render();
    this.fit();
  }

  moveWindow(x = 0, y = 0) {
    this.setPosition({
      x: this.slidingWindowPosition.x + x,
      y: this.slidingWindowPosition.y + y
    });
  }

  fit() {
    this.slidingWindowSize = this.getWindowSize();
    const boundaries = {
      left: Infinity,
      top: 0,
      right: 0,
      bottom: Infinity
    };

    Object.keys(this.nodes).forEach((key) => {
      boundaries.left = Math.min(boundaries.left, this.nodes[key].x);
      boundaries.right = Math.max(boundaries.right, this.nodes[key].x + NODE_WIDTH);
      boundaries.top = Math.max(boundaries.top, this.nodes[key].y + NODE_HEIGHT);
      boundaries.bottom = Math.min(boundaries.bottom, this.nodes[key].y);
    });

    this.setZoom(Math.min(
      (this.slidingWindowSize.width - OFFSET) / (boundaries.right - boundaries.left),
      (this.slidingWindowSize.height - OFFSET) / (boundaries.top - boundaries.bottom)
    ));

    this.setPosition({
      x: -this.scale * ((boundaries.left + boundaries.right) / 2) + (this.slidingWindowSize.width) / 2,
      y: -this.scale * ((boundaries.top + boundaries.bottom) / 2) + (this.slidingWindowSize.height) / 2
    })
  }

  centerZoom(zoomDelta) {
    const initialScale = this.scale;
    this.setZoom(this.scale + zoomDelta);
    const ratio = this.scale / initialScale;
    this.setPosition({
      x: this.slidingWindowSize.width/2 -(this.slidingWindowSize.width/2 - this.slidingWindowPosition.x) * ratio,
      y: this.slidingWindowSize.height/2- (this.slidingWindowSize.height/2 - this.slidingWindowPosition.y) * ratio
    })
  }
}

export default CanvasService;
