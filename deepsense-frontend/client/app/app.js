'use strict';

let angular = require('angular');
let browser = require('bowser');
let version = parseInt(browser.version.substring(0, 2), 10);

if (browser.chrome && version >= 40) {
  let lab = angular.module('ds.lab', [
    'ui.router',
    'ui.bootstrap',
    'ngSanitize',
    'ngCookies',
    'debounce',
    'ds.lab.partials',
    require('./common/deepsense-components/deepsense-attributes-panel/attributes-panel.module.js').name,
    require('./common/deepsense-components/deepsense-catalogue-panel/catalogue-panel.module.js').name,
    require('./common/deepsense-components/deepsense-cycle-analyser/deepsense-cycle-analyser.js').name,
    require('./common/deepsense-components/deepsense-graph-model/deepsense-graph-model.module.js').name,
    require('./common/deepsense-components/deepsense-loading-spinner/loading-spinner.module.js').name,
    require('./common/deepsense-components/deepsense-navigation-panel/deepsense-navigation-panel.js').name,
    require('./common/deepsense-components/deepsense-node-parameters/deepsense-node-parameters.module.js').name,
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
    '<div class="alert alert-danger no-support-message" role="alert">' +
    'We\'re sorry, Seahorse doesn\'t support your browser yet.<br/>' +
    'We\'re working on it, please use Google Chrome 40.0+ in the meantime.</div>';
}
