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

const _ = require('lodash');
const es6Rules = require('./es6.rules');
const angularRules = require('./angular.rules');
const srcRules = {
  env: {
    browser: true,
    jasmine: true
  },

  globals: {
    _: true,        // Should be imported when necessary
    $: true,        // Should be imported when necessary
    exports: true,
    jsPlumb: true,  // Should be imported when necessary
    jQuery: true,   // Should be imported when necessary
    module: true,
    moment: true,   // Should be imported when necessary
    require: true   // Refactor to use ES6 import
  },

  parserOptions: {
    sourceType: 'module'
  },

  root: true
};


module.exports = _.merge(es6Rules, angularRules, srcRules);
