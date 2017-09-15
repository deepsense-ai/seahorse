/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./graph-panel/graph-panel.module.js').inject(module);
  require('./freeze/freeze.js').inject(module);
};
