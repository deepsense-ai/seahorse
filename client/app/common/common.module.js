'use strict';

var angular = require('angular');

var common = angular.module('ds.common', []);

require('./api-clients/api-clients.module.js').inject(common);
require('./custom-scrollbar/common-custom-scrollbar.js').inject(common);
require('./filters/cut-words.js').inject(common);
require('./filters/precision.js').inject(common);
require('./focusElement/focus-element.js').inject(common);
require('./helpers/helpers.service.js').inject(common);
require('./loading-mask/loading-mask.module.js').inject(common);
require('./modals/modals.module.js').inject(common);
require('./resizable/resizable.js').inject(common);
require('./resizable/resizable-listener.js').inject(common);
require('./services/services.module.js').inject(common);
require('./sticky-window/sticky-window.js').inject(common);
require('./services/preset.service.js').inject(common);
require('./services/uuid-generator.js').inject(common);

module.exports = common;
