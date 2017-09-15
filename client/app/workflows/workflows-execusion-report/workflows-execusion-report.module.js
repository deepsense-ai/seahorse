'use strict';

exports.inject = function (module) {
  require('./workflows-execusion-report.controller.js').inject(module);
  require('./workflows-execusion-report.config.js').inject(module);
};
