/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./mouse-event.service.js').inject(module);
  require('./page.service.js').inject(module);
  require('./notification.service.js').inject(module);
};
