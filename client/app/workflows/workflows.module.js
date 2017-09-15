'use strict';

let angular = require('angular');

let workflows = angular.module('ds.workflows', []);

require('./workflows.config.js').inject(workflows);
require('./workflows.service.js').inject(workflows);
require('./common-behaviours/common-behaviours.module.js').inject(workflows);
require('./workflows-editor/workflows-editor.module.js').inject(workflows);
require('./workflows-execusion-report/workflows-execusion-report.module.js').inject(workflows);

module.exports = workflows;
