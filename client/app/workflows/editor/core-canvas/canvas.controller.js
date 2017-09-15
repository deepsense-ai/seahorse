'use strict';

class CanvasController {
  constructor(CanvasService, AdapterService, $element, $timeout) {
    'ngInject';
    this.CanvasService = CanvasService;
    this.AdapterService = AdapterService;
    this.$element = $element;
    this.$timeout = $timeout;

    this.portElement = null;
    this.portObject = null;
    this.isTooltipVisible = null;
  }

  $postLink() {
    const jsPlumbContainer = this.$element[0].querySelector('.flowchart-paint-area');
    const slidingWindow = this.$element[0].querySelector('.sliding-window');

    this.CanvasService.initialize(jsPlumbContainer, slidingWindow);
    this.AdapterService.setOnConnectionAbortFunction(this.onConnectionAbort);

    this.$timeout(() => {
      this.CanvasService.render();
      this.CanvasService.fit();
    }, 0);
  }

  $onChanges(changes) {
    if (changes.isEditable) {
      this.CanvasService.setEditable(changes.isEditable.currentValue);
    }

    if (changes.workflow) {
      this.CanvasService.setWorkflow(changes.workflow.currentValue);
    }

    if (changes.newNodeData) {
      this.AdapterService.setNewNodeData(changes.newNodeData.currentValue);
    }

    //this must be done in the next digest cycle because of ng-repeat are not available
    this.$timeout(() => {
      this.CanvasService.render();
    }, 0);
  }

}

export default CanvasController;
