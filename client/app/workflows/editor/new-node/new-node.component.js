'use strict';

import NewNodeController from './new-node.controller.js';
import NewNodeTemplate from './new-node.html';
import './new-node.less';

const NewNodeComponent = {
  bindings: {
    'data': '<'
  },
  controller: NewNodeController,
  templateUrl: NewNodeTemplate
};

export default NewNodeComponent;
