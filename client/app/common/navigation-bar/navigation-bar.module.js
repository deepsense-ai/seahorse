'use strict';

exports.inject = function (module) {
  require('./navigation-bar.drv.js').inject(module);
  require('./navigation-bar.ctrl.js').inject(module);
};
