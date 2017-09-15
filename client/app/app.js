'use strict';

var angular = require('angular');
var browser = require('detect-browser');

if (browser.name === 'chrome') {
  var lab = angular.module('ds.lab', [
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
  alert('Error: Unsupported browser.\nDetected browser: ' + _.capitalize(browser.name) + '\n' + 'Please use Chrome.');
}
