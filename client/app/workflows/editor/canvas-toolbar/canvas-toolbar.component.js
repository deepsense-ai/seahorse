'use strict';

import CanvasToolbarTemplate from './canvas-toolbar.html';
import CanvasToolbarController from './canvas-toolbar.controller.js';
import './canvas-toolbar.less';

const CanvasToolbarComponent = {
  bindings: {
    'onZoomIn': '&',
    'onZoomOut': '&',
    'onNewNode': '&',
    'onFullScreen': '&',
    'onFit': '&'
  },
  controller: CanvasToolbarController,
  templateUrl: CanvasToolbarTemplate
};

export default CanvasToolbarComponent;
