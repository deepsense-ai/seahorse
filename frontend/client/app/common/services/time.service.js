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

import moment from 'moment';

function TimeService() {
  this.getVerboseDateDiff = (date) => {
    let nowM = moment();
    let dateM = moment(date);
    let diff = nowM.diff(dateM);
    let diffSeconds = Math.floor(diff / 1000);
    let diffMinutes = Math.floor(diffSeconds / 60);
    let diffHours = Math.floor(diffMinutes / 60);
    let diffDays = Math.floor(diffHours / 24);

    if (diffSeconds < 60) {
      return 'less than a minute';
    } else if (diffMinutes < 60) {
      return diffMinutes === 1 ? 'a minute' : `${diffMinutes} minutes`;
    } else if (diffHours < 24) {
      return diffHours === 1 ? 'an hour' : `${diffHours} hours`;
    } else {
      return `${diffDays} days`;
    }
  };
}

exports.inject = function(module) {
  module.service('TimeService', TimeService);
};
