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

const attributesPanel = angular.module('deepsense.attributes-panel', [
  'deepsense.spinner',
  'deepsense.node-parameters',
  'ui.bootstrap',
  'xeditable',
  'NgSwitchery',
  'ui.ace',
  'angucomplete-alt'
]).run((editableOptions) => {
  editableOptions.theme = 'bs3';
});

require('./attribute-types/attribute-types.js');
require('./attributes-list/attributes-list.js');
require('./attributes-panel/attributes-panel.js');
require('./common/common.js');

module.exports = attributesPanel;
