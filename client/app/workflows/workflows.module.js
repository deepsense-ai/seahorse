'use strict';

let angular = require('angular');

let workflows = angular.module('ds.workflows', [
  require('./reports/reports.module.js').name
]);

require('./workflows.config.js').inject(workflows);
require('./workflows.service.js').inject(workflows);
require('./common-behaviours/common-behaviours.module.js').inject(workflows);
require('./graph-panel/graph-panel.module.js').inject(workflows);
require('./workflows-editor/workflows-editor.module.js').inject(workflows);
require('./workflows-execution-report/workflows-execution-report.module.js').inject(workflows);
require('./workflows-status-bar/workflows-status-bar.drv.js').inject(workflows);
require('./workflows-status-bar/workflows-status-bar.ctrl.js').inject(workflows);
require('./workflows-status-bar/workflows-editor-status-bar.service.js').inject(workflows);
require('./general-data-panel/general-data-panel.js').inject(workflows);
require('./general-data-panel/general-data-panel.ctrl.js').inject(workflows);
require('./general-data-panel/general-data-text-area.js').inject(workflows);

require('./workflows-status-bar/menu-item/menu-item.drv.js').inject(workflows);
require('./workflows-status-bar/menu-item/menu-item.ctrl.js').inject(workflows);

module.exports = workflows;
