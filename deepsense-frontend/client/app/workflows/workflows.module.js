'use strict';

let angular = require('angular');

let workflows = angular.module('ds.workflows', [
  require('./reports/reports.module.js').name
]);

require('./workflows.config.js').inject(workflows);
require('./workflows.service.js').inject(workflows);
require('./common-behaviours/common-behaviours.module.js').inject(workflows);
require('./graph-panel/graph-panel.module.js').inject(workflows);
require('./inner-workflows/public-param/public-params-list.js').inject(workflows);
require('./inner-workflows/default-inner-workflow-generator.service.js').inject(workflows);
require('./workflows-editor/workflows-editor.module.js').inject(workflows);
require('./workflows-status-bar/workflows-status-bar.drv.js').inject(workflows);
require('./workflows-status-bar/workflows-status-bar.ctrl.js').inject(workflows);
require('./workflows-status-bar/workflows-editor-status-bar.service.js').inject(workflows);
require('./general-data-panel/general-data-panel.js').inject(workflows);
require('./general-data-panel/general-data-text-area.js').inject(workflows);
require('./copy-paste/copy-paste.js').inject(workflows);
require('./workflows-status-bar/menu-item/menu-item.directive.js').inject(workflows);
require('./workflows-status-bar/menu-item/menu-item.controller.js').inject(workflows);
require('./workflows-status-bar/additional-html/running-executor-popover.ctrl.js').inject(workflows);
require('./workflows-status-bar/additional-html/starting-popover.ctrl.js').inject(workflows);
require('./workflows-status-bar/additional-html/executor-error.ctrl.js').inject(workflows);
require('./workflows-editor/side-bar/side-bar.directive.js').inject(workflows);
require('./workflows-editor/side-bar/side-bar.controller.js').inject(workflows);
require('./workflows-editor/side-bar/side-bar.service.js').inject(workflows);
require('./workflows-editor/bottom-bar/bottom-bar.directive.js').inject(workflows);
require('./workflows-editor/bottom-bar/bottom-bar.controller.js').inject(workflows);
require('./workflows-editor/bottom-bar/bottom-bar.service.js').inject(workflows);
require('./session-manager.service.js').inject(workflows);
require('./navigation-bar/navigation-bar.module.js').inject(workflows);

module.exports = workflows;
