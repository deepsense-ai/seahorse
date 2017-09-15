'use strict';

import CanvasTemplate from './canvas.html';
import './canvas.less';

const CanvasComponent = {
  bindings: {
    'isEditable': '<',
    'newNodeData': '<',
    'workflow': '<',
    'onConnectionAbort': '&'
  },
  controller: class CanvasController {
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
      });
    }

    $onChanges(changes) {
      if (changes.isEditable) {
        this.CanvasService.setEditable(changes.isEditable.currentValue);
      }

      if (changes.workflow) {
        this.CanvasService.setWorkflow(changes.workflow.currentValue);
        this.CanvasService.fit();
      }

      if (changes.newNodeData) {
        this.AdapterService.setNewNodeData(changes.newNodeData.currentValue);
      }

      //this must be done in the next digest cycle because of ng-repeat are not available
      this.$timeout(() => {
        this.CanvasService.render();
      });
    }

  },
  templateUrl: CanvasTemplate,
  transclude: true
};

export default CanvasComponent;
