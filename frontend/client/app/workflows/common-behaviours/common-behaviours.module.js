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
  require('./common-jsplumb-draggable.js').inject(module);
  require('./common-draggable.js').inject(module);
  require('./common-droppable.js').inject(module);
  require('./common-drag-and-drop.service.js').inject(module);
  require('./common-keyboard.js').inject(module);
  require('./common-stick-onscroll.js').inject(module);
  require('./common-focused.js').inject(module);
  require('./common-flowchart-box-undraggable.js').inject(module);
  require('./common-context-menu-blocker.js').inject(module);
  require('./common-calculate-available-view-height.js').inject(module);
};
