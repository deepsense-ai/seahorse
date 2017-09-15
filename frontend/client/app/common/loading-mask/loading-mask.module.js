'use strict';

exports.inject = function(module) {
  require('./loading-mask.drv.js').inject(module);
  require('./loading-mask.ctrl.js').inject(module);
};
