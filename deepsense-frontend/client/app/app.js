'use strict';

let angular = require('angular');
let browser = require('bowser');
let version = parseInt(browser.version.substring(0, 2));

if (browser.chrome && version >= 40) {
  let lab = angular.module('ds.lab', [
    'ui.router',
    'ui.bootstrap',
    'ngSanitize',
    'ngCookies',
    'debounce',
    'ds.lab.partials',
    'deepsense.spinner',
    'deepsense-catalogue-panel',
    'deepsense.attributes-panel',
    'deepsense.navigation-panel',
    'deepsense.graph-model',
    'deepsense.cycle-analyser',
    'ngFileUpload',
    'toastr',
    require('./home/home.module.js').name,
    require('./workflows/workflows.module.js').name,
    require('./enums/enums.module.js').name,
    require('./common/common.module.js').name,
    require('./errors/errors.module.js').name,
    require('./server-communication/server-communication.module.js').name
  ]);
  require('./app.config.js').inject(lab);
  require('./version.factory.js').inject(lab);
  require('./UserService.js').inject(lab);
  require('./app.run.js').inject(lab);
} else {
  document.body.innerHTML =
    '<div class="alert alert-danger" role="alert" style="font-size: 18px;">' +
    'We\'re sorry, Seahorse doesn\'t support your browser yet.<br/>' +
    'We\'re working on it, please use Google Chrome 40.0+ in the meantime.</div>';
}
