'use strict';

exports.inject = function(module) {
  require('./agreement-modal.drv.js').inject(module);
  require('./agreement-modal.ctrl.js').inject(module);
};
