'use strict';

exports.inject = function(module) {
  require('./graph-panel-node/graph-panel-node.module.js').inject(module);
  require('./port-statuses-tooltip/port-statuses-tooltip.controller.js').inject(module);
  require('./graph-panel-flowchart.js').inject(module);
  require('./graph-panel-renderer/graph-panel-renderer.service.js').inject(module);
  require('./graph-panel-renderer/connection-hinter.factory.js').inject(module);
  require('./multi-selection/multi-selection.js').inject(module);
  require('./multi-selection/multi-selection.service.js').inject(module);
};
