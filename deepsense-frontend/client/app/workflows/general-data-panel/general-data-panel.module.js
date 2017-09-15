'use strict';

exports.inject = function (module) {
  require('./general-data.panel.drv.js').inject(module);
  require('./general-data-panel.ctrl.js').inject(module);
};
