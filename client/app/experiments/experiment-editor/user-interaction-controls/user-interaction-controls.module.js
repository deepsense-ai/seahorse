/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./user-interaction-controls.js').inject(module);

  require('./zoom/zoom.js').inject(module);
  require('./move/move.js').inject(module);
  require('./defaultZoom/defaultZoom.js').inject(module);
  require('./fit/fit.js').inject(module);
};
