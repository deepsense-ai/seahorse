'use strict';

import NewNodeTemplate from './new-node.html';
import './new-node.less';

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
      this.$element.bind('mousedown click', (event) => {
        event.stopPropagation();
      });

      this.$timeout(() => {
        const {top, left, right, bottom} = this.$element[0].getBoundingClientRect();
        const rect = {top, left, right, bottom};
        this.onDisplayRectChange({rect});
      }, 0);
    }
  },
  templateUrl: NewNodeTemplate
};

export default NewNodeComponent;
