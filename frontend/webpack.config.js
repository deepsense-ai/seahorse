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

/* eslint-disable no-console */

'use strict';

const _ = require('lodash');
const _configs = {

  // global section
  global: require(__dirname + '/config/webpack/global'),

  // config by environments
  production: require(__dirname + '/config/webpack/production'),
  development: require(__dirname + '/config/webpack/development')
};

const _load = function() {
  const ENV = process.env.NODE_ENV || 'production';

  console.log('Current Environment: ', ENV);

  // load config file by environment
  return _configs && _.merge(
      _configs.global(__dirname),
      _configs[ENV](__dirname)
    );
};

module.exports = _load();
