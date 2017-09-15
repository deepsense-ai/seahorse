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

import jqCronDirective from './jqcron/jqcron.directive';

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
require('./services/preset.service.js').inject(common);
require('./services/uuid-generator.js').inject(common);

common
  .directive('jqCron', jqCronDirective);


module.exports = common;
