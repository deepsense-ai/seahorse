/**controller
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./graph-panel-node/graph-panel-node.module.js').inject(module);

  require('./port-statuses-tooltip/port-statuses-tooltip.controller.js').inject(module);

  require('./graph-panel-flowchart.js').inject(module);
  require('./graph-panel-renderer.service.js').inject(module);

  require('./graph-panel-freeze.service.js').inject(module);

  require('./multi-selection/multi-selection.js').inject(module);
};
