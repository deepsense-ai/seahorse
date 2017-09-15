'use strict';

let angular = require('angular');

let workflows = angular.module('ds.workflows', [
  require('./reports/reports.module.js').name,
  require('./editor/editor.module.js')
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
require('./workflows-status-bar/documentation-link/documentation-link.directive.js').inject(workflows);
require('./workflows-status-bar/selection-items/selection-items.controller.js').inject(workflows);
require('./workflows-status-bar/selection-items/selection-items.directive.js').inject(workflows);
require('./general-data-panel/general-data-panel.module.js').inject(workflows);
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
require('./cluster-settings-modals/choose-cluster-modal.ctrl.js').inject(workflows);
require('./cluster-settings-modals/cluster-modal.srv.js').inject(workflows);
require('./cluster-settings-modals/preset-modal/preset-modal.controller.js').inject(workflows);
require('./cluster-settings-modals/preset-modal/preset-modal-labels.js').inject(workflows);
require('./dataframe-library-modal/dataframe-library-modal.ctrl.js').inject(workflows);
require('./dataframe-library-modal/dataframe-library-modal.srv.js').inject(workflows);
require('./dataframe-library-modal/file-upload-change.drv.js').inject(workflows);
require('./dataframe-library-modal/dropzone-file-upload.drv.js').inject(workflows);

module.exports = workflows;
