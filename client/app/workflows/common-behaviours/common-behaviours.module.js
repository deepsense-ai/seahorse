/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./common-jsplumb-draggable.js').inject(module);
  require('./common-draggable.js').inject(module);
  require('./common-droppable.js').inject(module);
  require('./common-drag-and-drop.service.js').inject(module);
  require('./common-render-finish.js').inject(module);
  require('./common-keyboard.js').inject(module);
  require('./common-stick-onscroll.js').inject(module);
  require('./common-context-menu-blocker.js').inject(module);
  require('./common-custom-scrollbar.js').inject(module);
  require('./common-focused.js').inject(module);
};
