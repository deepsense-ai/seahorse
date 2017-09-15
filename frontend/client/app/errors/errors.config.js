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

import errorMissingTpl from './error-missing.html';
import errorVersionTpl from './error-version.html';
import errorTimeoutTpl from './error-request-timeout.html';

/* @ngInject */
function ErrorsConfig($stateProvider) {
  $stateProvider.state('MissingState', {
    url: '/error/missing',
    templateUrl: errorMissingTpl,
    controller: 'ErrorController as controller'
  });

  $stateProvider.state('ConflictState', {
    url: '/:type/error/version/:id/',
    params: {
      id: undefined,
      type: undefined,
      errorMessage: undefined
    },
    templateUrl: errorVersionTpl,
    controller: 'ErrorController as controller'
  });

  $stateProvider.state('RequestTimeout', {
    url: '/error/request-timeout',
    templateUrl: errorTimeoutTpl,
    controller: 'ErrorController as controller'
  });
}

exports.function = ErrorsConfig;

exports.inject = function(module) {
  module.config(ErrorsConfig);
};
