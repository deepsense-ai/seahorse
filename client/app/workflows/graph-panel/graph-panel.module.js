'use strict';

exports.inject = function(module) {
  require('./graph-panel-flowchart.js').inject(module);
  require('./graph-panel-flowchart.ctrl.js').inject(module);
  require('./graph-panel-renderer/graph-panel-renderer.service.js').inject(module);
  require('./graph-panel-renderer/graph-panel-styler.js').inject(module);
  require('./multi-selection/multi-selection.js').inject(module);
  require('./multi-selection/multi-selection.service.js').inject(module);
};
