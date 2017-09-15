/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var angular = require('angular');

/**
 * DesspSense.io LAB main module.
 */
var lab = angular.module('ds.lab', [
  'ui.router',
  'ui.bootstrap',
  'debounce',
  'deepsense.spinner',
  'deepsense-context-menu',
  'deepsense-catalogue-panel',
  'deepsense.attributes-panel',
  'deepsense.navigation-panel',
  'deepsense.graph-model',
  'deepsense.cycle-analyser',
  'toastr',
  require('./home/home.module.js').name,
  require('./account/account.module.js').name,
  require('./experiments/experiments.module.js').name,
  require('./datasets/datasets.module.js').name,
  require('./common/common.module.js').name,
  require('./reports/reports.module.js').name,
  require('./errors/errors.module.js').name
]);

require('./app.config.js').inject(lab);
require('./app.run.js').inject(lab);
