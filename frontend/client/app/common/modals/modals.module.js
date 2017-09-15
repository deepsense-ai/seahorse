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

exports.inject = function(module) {
  require('./confirmation-modal/confirmation-modal.ctrl.js').inject(module);
  require('./confirmation-modal/confirmation-modal.service.js').inject(module);
  require('./delete-modal/delete-modal.ctrl.js').inject(module);
  require('./delete-modal/delete-modal.service.js').inject(module);
  require('./export-modal/export-modal.ctrl.js').inject(module);
  require('./export-modal/export-modal.service.js').inject(module);
  require('./new-workflow-modal/new-workflow-modal.ctrl.js').inject(module);
  require('./upload-workflow-modal/upload-workflow-modal.ctrl.js').inject(module);
  require('./workflow-clone-modal/workflow-clone-modal.srv.js').inject(module);
  require('./workflow-clone-modal/workflow-clone-modal.ctrl.js').inject(module);
};
