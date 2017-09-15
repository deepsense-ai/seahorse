/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./experiment-browser.js').inject(module);
  require('./experiment-crud.ctrl.js').inject(module);
  require('./experiment-browser-top-toolbar/experiment-browser-top-toolbar.js').inject(module);

  require('./experiment-crud-modal/experiment-crud-add-modify-modal/experiment-crud-add-modify-modal.ctrl.js').inject(module);
  require('./experiment-crud-modal/experiment-crud-delete-modal/experiment-crud-delete-modal.ctrl.js').inject(module);
};
