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

function HelpersService() {
  'use strict';

  this.sliding = function sliding(array, slidingWindow) {
    let iterations = array.length - slidingWindow + 1;
    return _.range(0, iterations).map((i) => array.slice(i, i + slidingWindow));
  };
}

exports.function = HelpersService;

exports.inject = function(module) {
  'use strict';

  module.service('HelpersService', HelpersService);
};
