'use strict';

exports.inject = function (module) {
  require('./workflows-execution-report.controller.js').inject(module);
  require('./workflows-execution-report.config.js').inject(module);
};
