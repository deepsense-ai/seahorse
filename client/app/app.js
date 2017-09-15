'use strict';

let angular = require('angular');
let browserValidator = require('./browser.validator.js');

import "../css/app.less";

if (browserValidator.isBrowserSupported()) {
  let lab = angular.module('ds.lab', [
    'ui.router',
    'ui.bootstrap',
    'ngSanitize',
    'ngCookies',
    'rt.debounce',
    require('./common/deepsense-components/deepsense-attributes-panel/attributes-panel.module.js').name,
    require('./common/deepsense-components/deepsense-cycle-analyser/deepsense-cycle-analyser.js').name,
    require('./common/deepsense-components/deepsense-graph-model/deepsense-graph-model.module.js').name,
    require('./common/deepsense-components/deepsense-loading-spinner/loading-spinner.module.js').name,
    require('./common/deepsense-components/deepsense-node-parameters/deepsense-node-parameters.module.js').name,
    'ngFileUpload',
    'toastr',
    require('./home/home.module.js').name,
    require('./workflows/workflows.module.js').name,
    require('./common/common.module.js').name,
    require('./errors/errors.module.js').name,
    require('./server-communication/server-communication.module.js').name
  ]);
  require('./app.config.js').inject(lab);
  require('./version.factory.js').inject(lab);
  require('./UserService.js').inject(lab);
  require('./app.run.js').inject(lab);
} else {
  document.addEventListener("DOMContentLoaded", function() {
    document.body.innerHTML = browserValidator.getErrorMessageHTML();
  });
  angular.module('ds.lab', []); //so config is not throwing exceptions that ds.lab is not available
}


