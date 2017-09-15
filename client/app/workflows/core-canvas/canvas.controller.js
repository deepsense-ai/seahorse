'use strict';

class CanvasController {
  /*@ngInject*/
  constructor(CanvasService, $element, $timeout) {
    this.CanvasService = CanvasService;
    this.$element = $element;
    this.$timeout = $timeout;
  }

  $postLink() {
    const jsPlumbContainer = this.$element[0].querySelector('.flowchart-paint-area');
    const slidingWindow = this.$element[0].querySelector('.sliding-window');

    $(window).keypress((event) => {
      switch (event.key) {
        case 'w':
          this.CanvasService.moveWindow(0, -this.CanvasService.moveStep);
          break;
        case 's':
          this.CanvasService.moveWindow(0, this.CanvasService.moveStep);
          break;
        case 'a':
          this.CanvasService.moveWindow(this.CanvasService.moveStep, 0);
          break;
        case 'd':
          this.CanvasService.moveWindow(-this.CanvasService.moveStep, 0);
          break;
        case 'q':
          this.CanvasService.zoomIn();
          break;
        case 'e':
          this.CanvasService.zoomOut();
          break;
        case 'f':
          this.CanvasService.fit();
          break;
        case 'g':
          this.CanvasService.render();
          break;
        //no-default
      }
    });

    this.CanvasService.initialize(jsPlumbContainer, slidingWindow);
    this.CanvasService.setCollection(this.workflow.getNodes());
    this.CanvasService.setEdges(this.workflow.getEdges());

    //this must be done in the next digest cycle because of ng-repeat are not available
    this.$timeout(()=>{
      this.CanvasService.render();
    }, 0);
  }
}

export default CanvasController;
