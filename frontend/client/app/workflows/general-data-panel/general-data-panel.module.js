'use strict';

exports.inject = function (module) {
  require('./general-data.panel.directive.js').inject(module);
  require('./general-data-panel.controller.js').inject(module);
};
