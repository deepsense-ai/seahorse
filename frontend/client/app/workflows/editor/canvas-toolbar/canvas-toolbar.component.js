'use strict';

import CanvasToolbarTemplate from './canvas-toolbar.html';
import './canvas-toolbar.less';

const CanvasToolbarComponent = {
  bindings: {
    'isEditable': '<',
    'onZoomIn': '&',
    'onZoomOut': '&',
    'onNewNode': '&',
    'onFullScreen': '&',
    'onFit': '&'
  },
  controller: class CanvasToolbarController {
    constructor($element, $timeout) {
      'ngInject';

      this.$element = $element;
      this.$timeout = $timeout;

      this.$ghost = $('<div class="graph-node standard border-default"></div>');
    }

    $postLink() {
      this.$timeout(() => {
        this.$nodeDragElement = $(this.$element[0].querySelector('.drag-node'));
        this.$nodeDragElement.on('dragstart', (event) => this.onDragStart(event));
        this.$nodeDragElement.on('dragend', (event) => this.onDragEnd(event));
      }, 0);
    }

    $onDestroy() {
      this.$nodeDragElement.off();
    }

    onDragStart(event) {
      // When trying to append to this.$element - it created ugly artifacts. With appending to body works fine.
      $('body').append(this.$ghost);
      event.originalEvent.dataTransfer.setDragImage(this.$ghost[0], 0, 0);
    }

    onDragEnd() {
      this.$ghost.remove();
    }

  },
  templateUrl: CanvasToolbarTemplate
};

export default CanvasToolbarComponent;
