'use strict';

var angular = require('angular');

var common = angular.module('ds.common', []);

require('./navigation-bar/navigation-bar.module.js').inject(common);
require('./api-clients/api-clients.module.js').inject(common);
require('./services/services.module.js').inject(common);
require('./sticky-window/sticky-window.js').inject(common);
require('./status-bar/status-bar.js').inject(common);
require('./services/uuid-generator.js').inject(common);
require('./modals/modals.module.js').inject(common);

module.exports = common;
