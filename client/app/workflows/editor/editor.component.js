'use strict';

import EditorController from './editor.controller.js';
import EditorTemplate from './editor.html';
import './editor.less';

const EditorComponent = {
  bindings: {
    'isEditable' : '<',
    'workflow': '<'
  },
  controller: EditorController,
  templateUrl: EditorTemplate
};

export default EditorComponent;
