/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./navigation-bar.drv.js').inject(module);
  require('./navigation-bar.ctrl.js').inject(module);
};
