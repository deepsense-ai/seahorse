'use strict';

import NewNodeTemplate from './new-node.html';
import './new-node.less';

const NewNodeComponent = {
  bindings: {
    'data': '<',
    'onSelect': '&'
  },
  controller: class NewNodeController {
    constructor($element) {
      'ngInject';
      this.$element = $element;
    }
    $postLink() {
      this.$element.bind('mousedown', (event) => {
        event.stopPropagation();
      });
    }
  },
  templateUrl: NewNodeTemplate
};

export default NewNodeComponent;
