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

require('./experiment-browser/experiment-browser.module.js').inject(experiments);

require('./utils/uuid-generator.js').inject(experiments);

require('./common-behaviours/common-behaviours.module.js').inject(experiments);

require('./experiment-editor/experiment-editor.module.js').inject(experiments);

module.exports = experiments;
