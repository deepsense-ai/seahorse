'use strict';

class CanvasController {
  constructor(CanvasService, NewNodeService, $element, $timeout) {
    'ngInject';
    this.CanvasService = CanvasService;
    this.NewNodeService = NewNodeService;
    this.$element = $element;
    this.$timeout = $timeout;
  }

  $postLink() {
    const jsPlumbContainer = this.$element[0].querySelector('.flowchart-paint-area');
    const slidingWindow = this.$element[0].querySelector('.sliding-window');
    const canvasContainer = this.$element[0].querySelector('.flowchart-box');

    this.CanvasService.initialize(jsPlumbContainer, slidingWindow);
    this.NewNodeService.initialize(canvasContainer);

    //this must be done in the next digest cycle because of ng-repeat are not available
    this.$timeout(() => {
      this.CanvasService.render();
    }, 0);
  }

  $onChanges(changes) {
    if (changes.isEditable) this.CanvasService.setEditable(changes.isEditable.currentValue);
    if (changes.workflow) this.CanvasService.setWorkflow(changes.workflow.currentValue);

    //this must be done in the next digest cycle because of ng-repeat are not available
    this.$timeout(() => {
      this.CanvasService.render();
    }, 0);
  }
}

export default CanvasController;
