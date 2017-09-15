/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
require('./workflows-editor/bottom-bar/bottom-bar.directive.js').inject(workflows);
require('./workflows-editor/bottom-bar/bottom-bar.controller.js').inject(workflows);
require('./workflows-editor/bottom-bar/bottom-bar.service.js').inject(workflows);
require('./session-manager.service.js').inject(workflows);
require('./navigation-bar/navigation-bar.module.js').inject(workflows);
require('./cluster-settings-modals/choose-cluster-modal.ctrl.js').inject(workflows);
require('./cluster-settings-modals/cluster-modal.srv.js').inject(workflows);
require('./cluster-settings-modals/preset-modal/preset-modal.controller.js').inject(workflows);
require('./cluster-settings-modals/preset-modal/preset-modal-labels.js').inject(workflows);
require('./library/library-modal.controller.js').inject(workflows);
require('./library/library-modal.service.js').inject(workflows);
require('./library/file-upload-section/file-upload-change.directive.js').inject(workflows);
require('./library/file-upload-section/dropzone-file-upload.directive.js').inject(workflows);

module.exports = workflows;
