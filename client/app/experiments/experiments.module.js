/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var angular = require('angular');

var experiments = angular.module('ds.experiments', []);

require('./experiment.config.js').inject(experiments);
require('./experiments.config.js').inject(experiments);

require('./experiment.controller.js').inject(experiments);
require('./experiment.service.js').inject(experiments);
require('./experiment-browser/experiment-browser.js').inject(experiments);
require('./common-behaviours/common-draggable.js').inject(experiments);
require('./common-behaviours/common-droppable.js').inject(experiments);
require('./common-behaviours/common-drop-target.js').inject(experiments);
require('./common-behaviours/common-render-finish.js').inject(experiments);
require('./common-behaviours/common-keyboard.js').inject(experiments);
require('./experiment-editor/context-menu/context-menu.js').inject(experiments);
require('./experiment-editor/context-menu/context-menu-element.js').inject(experiments);
require('./experiment-editor/port-statuses-tooltip/port-statuses-tooltip.controller.js').inject(experiments);
require('./experiment-editor/status-bar/status-bar.js').inject(experiments);
require('./experiment-editor/graph-panel/graph-panel-flowchart.js').inject(experiments);
require('./experiment-editor/graph-panel/graph-panel-node.js').inject(experiments);
require('./experiment-editor/graph-panel/graph-panel-renderer.service.js').inject(experiments);
require('./experiment-editor/catalog-panel/catalog-panel.js').inject(experiments);
require('./experiment-editor/catalog-panel/catalog-panel-operation.js').inject(experiments);
require('./experiment-editor/attributes-panel/attributes-panel.module.js').inject(experiments);
require('./utils/uuid-generator.js').inject(experiments);
require('./common-behaviours/common-context-menu-blocker.js').inject(experiments);
require('./experiment-editor/user-interaction-controls/user-interaction-controls.js').inject(experiments);
require('./experiment-editor/user-interaction-controls/zoom/zoom.js').inject(experiments);
require('./experiment-editor/user-interaction-controls/move/move.js').inject(experiments);
require('./experiment-editor/user-interaction-controls/defaultZoom/defaultZoom.js').inject(experiments);
require('./experiment-editor/user-interaction-controls/fit/fit.js').inject(experiments);

module.exports = experiments;
