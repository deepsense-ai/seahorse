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

/* @ngInject */
function AppConfig($urlRouterProvider, toastrConfig, $cookiesProvider) {
  angular.extend(toastrConfig, {
    'allowHtml': true,
    'newestOnTop': false,
    'positionClass': 'toast-bottom-left',
    'closeButton': true,
    'progressBar': true,
    'timeOut': 3500,
    'maxOpened': 5,
    'iconClasses': {
      'error': 'notification--error fa-exclamation-circle',
      'info': 'toast-info',
      'success': 'toast-success',
      'warning': 'toast-warning'
    }
  });
  $urlRouterProvider.otherwise('/');

  const expiresDate = new Date();
  expiresDate.setFullYear(expiresDate.getFullYear() + 2);
  $cookiesProvider.defaults.expires = expiresDate;
}

exports.inject = function(module) {
  module.config(AppConfig);
};
