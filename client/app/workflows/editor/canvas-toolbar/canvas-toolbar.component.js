'use strict';

import CanvasToolbarTemplate from './canvas-toolbar.html';
import './canvas-toolbar.less';

const CanvasToolbarComponent = {
  require: {
    'editor': '^editor'
  },
  templateUrl: CanvasToolbarTemplate
};

export default CanvasToolbarComponent;
