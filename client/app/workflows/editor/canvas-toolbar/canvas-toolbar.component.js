'use strict';

import CanvasToolbarTemplate from './canvas-toolbar.html';
import './canvas-toolbar.less';

const CanvasToolbarComponent = {
  bindings: {
    'onZoomIn': '&',
    'onZoomOut': '&',
    'onNewNode': '&',
    'onFullScreen': '&',
    'onFit': '&'
  },
  templateUrl: CanvasToolbarTemplate
};

export default CanvasToolbarComponent;
