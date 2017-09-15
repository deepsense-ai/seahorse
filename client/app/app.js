'use strict';

let angular = require('angular');
let browser = require('detect-browser');
let unsupported = require('./unsupported.js');
let version = parseInt(browser.version.substring(0, 2));

if (browser.name === 'chrome' && version >= 40) {
  let lab = angular.module('ds.lab', [
    'ui.router',
    'ui.bootstrap',
    'ngSanitize',
    'debounce',
    'ds.lab.partials',
    'deepsense.spinner',
    'deepsense-context-menu',
    'deepsense-catalogue-panel',
    'deepsense.attributes-panel',
    'deepsense.navigation-panel',
    'deepsense.graph-model',
    'deepsense.cycle-analyser',
    'ngFileUpload',
    'toastr',
    require('./home/home.module.js').name,
    require('./workflows/workflows.module.js').name,
    require('./common/common.module.js').name,
    require('./errors/errors.module.js').name
  ]);
  lab.constant('additionalControls', false);
  require('./app.config.js').inject(lab);
  require('./app.run.js').inject(lab);
} else {
  let lab = angular.module('ds.lab', ['ui.router']);
  unsupported.inject(lab);
}
