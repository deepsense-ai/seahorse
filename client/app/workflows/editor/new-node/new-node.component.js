'use strict';

import NewNodeTemplate from './new-node.html';
import './new-node.less';

const NEW_NODE_WIDTH = 290;
const NEW_NODE_HEIGHT = 295;

const NewNodeComponent = {
  bindings: {
    'categories': '<',
    'containment': '<',
    'data': '<',
    'onDisplayRectChange': '&',
    'onSelect': '&'
  },
  controller: class NewNodeController {
    constructor($element, $timeout) {
      'ngInject';
      this.$element = $element;
      this.$timeout = $timeout;
    }
    $postLink() {
      this.$element.bind('mousedown', (event) => {
        event.stopPropagation();
      });

      this.$timeout(() => {
        const {top, left} = this.$element[0].getBoundingClientRect();
        const rect = {
          top,
          left,
          right: left + NEW_NODE_WIDTH,
          bottom: top + NEW_NODE_HEIGHT
        };
        this.onDisplayRectChange({rect});
      }, 0);
    }
  },
  templateUrl: NewNodeTemplate
};

export default NewNodeComponent;
