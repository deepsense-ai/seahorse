'use strict';

exports.inject = function(module) {
  require('./multi-selection/multi-selection.js').inject(module);
  require('./multi-selection/multi-selection.service.js').inject(module);
};
