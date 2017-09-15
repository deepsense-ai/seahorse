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

function removeTrailingZeros(str) {
  // if no decimal or scientific notation used, return unchanged
  if (!_.includes(str, '.') || _.includes(str, 'e') || _.includes(str, 'E')) {
    return str;
  }
  //  remove trailing zeros
  str = str.replace(/0+$/, '');
  // if number ends with decimal, remove it too
  if (str.charAt(str.length - 1) === '.') {
    str = str.slice(0, -1);
  }
  return str;
}


function precision() {
  const DEFAULT_PRECISION = 6;

  return function(value, digits) {
    if (_.includes(value, 'e') || _.includes(value, 'E')) {
      return value;
    }

    const numVal = parseFloat(value);
    if (_.isNaN(numVal)) {
      return value;
    }

    if (_.isUndefined(digits)) {
      digits = DEFAULT_PRECISION;
    }

    return removeTrailingZeros(numVal.toPrecision(digits));
  };
}

exports.inject = function(module) {
  module.filter('precision', precision);
};
