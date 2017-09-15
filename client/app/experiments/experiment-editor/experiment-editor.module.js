/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./port-statuses-tooltip/port-statuses-tooltip.controller.js').inject(module);

  require('./status-bar/status-bar.js').inject(module);

  require('./graph-panel/graph-panel-flowchart.js').inject(module);
  require('./graph-panel/graph-panel-node.js').inject(module);
  require('./graph-panel/graph-panel-renderer.service.js').inject(module);

  require('./catalog-panel/catalog-panel.js').inject(module);
  require('./catalog-panel/catalog-panel-operation.js').inject(module);

  require('./attributes-panel/attributes-panel.module.js').inject(module);

  require('./user-interaction-controls/user-interaction-controls.module.js').inject(module);
};
