/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./context-menu/report-options.service.js').inject(module);

  require('./graph-panel-node.js').inject(module);
};
