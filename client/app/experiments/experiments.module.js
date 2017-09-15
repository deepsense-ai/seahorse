/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var angular = require('angular');

var experiments = angular.module('ds.experiments', []);

require('./experiment.controller.js').inject(experiments);
require('./experiments.config.js').inject(experiments);
require('./experiment.factory.js').inject(experiments);
require('./experiment-browser/experiment-browser.js').inject(experiments);
require('./common-behaviours/common-draggable.js').inject(experiments);
require('./common-behaviours/common-render-finish.js').inject(experiments);
require('./experiment-editor/graph-panel/graph-panel-flowchart.js').inject(experiments);
require('./experiment-editor/graph-panel/graph-panel-node.js').inject(experiments);
require('./experiment-editor/graph-panel/graph-panel-renderer.js').inject(experiments);
require('./experiment-editor/catalog-panel/catalog-panel.js').inject(experiments);
require('./experiment-editor/catalog-panel/catalog-panel-operation.js').inject(experiments);
require('./experiment-editor/attributes-panel/attributes-panel.js').inject(experiments);
require('./experiment-editor/attributes-panel/attributes-value-view.js').inject(experiments);

module.exports = experiments;
