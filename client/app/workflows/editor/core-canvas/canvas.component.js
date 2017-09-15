'use strict';

import CanvasController from './canvas.controller.js';
import CanvasTemplate from './canvas.html';
import './canvas.less';

const CanvasComponent = {
  bindings: {
    'isEditable' : '<',
    'newNodeData': '<',
    'workflow': '<',
    'onConnectionAbort': '&'
  },
  controller: CanvasController,
  templateUrl: CanvasTemplate,
  transclude: true
};

export default CanvasComponent;
