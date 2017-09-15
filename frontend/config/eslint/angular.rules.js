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

module.exports = {
  globals: {
    angular: true  // Should be imported when necessary
  },

  plugins: [
    'angular'
  ],

  rules: {
    // {{{ Angular

    // {{{ Angular : Possible Errors
    // 'angular/module-getter': 'error', // disallow to reference modules with variables and require to use the getter syntax instead angular.module('myModule') (y022)
    // 'angular/module-setter': 'error', // disallow to assign modules to variables (linked to module-getter (y021)
    // 'angular/no-private-call': 'error', // disallow use of internal angular properties prefixed with $$
    // Angular : Possible Errors }}}

    // {{{ Angular : Best Practices
    // 'angular/component-limit': 'error', // limit the number of angular components per file (y001)
    // 'angular/controller-as-route' // require the use of controllerAs in routes or states (y031)
    // 'angular/controller-as-vm' // require and specify a capture variable for this in controllers (y032)
    // 'angular/controller-as' // disallow assignments to $scope in controllers (y031)
    // 'angular/deferred': 'error', // use $q(function(resolve, reject){}) instead of $q.deferred
    // 'angular/di-unused': 'error', // disallow unused DI parameters
    'angular/directive-restrict': 'error', // disallow any other directive restrict than 'A' or 'E' (y074)
    'angular/empty-controller': 'error', // disallow empty controllers
    // 'angular/no-controller' // disallow use of controllers (according to the component first pattern)
    'angular/no-inline-template': 'error', // disallow the use of inline templates
    'angular/no-run-logic': 'error', // keep run functions clean and simple (y171)
    'angular/no-services': 'error', // disallow DI of specified services for other angular components ($http for controllers, filters and directives)
    // 'angular/on-watch': 'error', // require $on and $watch deregistration callbacks to be saved in a variable
    // 'angular/prefer-component' //
    // Angular : Best Practices }}}

    // {{{ Angular : Deprecated Angular Features
    'angular/no-cookiestore': 'error', // use $cookies instead of $cookieStore
    // 'angular/no-directive-replace': 'warn', // disallow the deprecated directive replace property
    'angular/no-http-callback': 'error', // disallow the $http methods success() and error()
    // Angular : Deprecated Angular Features }}}

    // {{{ Angular : Naming
    // 'angular/component-name': ['error', 'ds'], // require and specify a prefix for all component names
    // 'angular/controller-name': 'error', // require and specify a prefix for all controller names (y123, y124)
    // 'angular/directive-name': ['error', 'ds'], // require and specify a prefix for all directive names (y073, y126)
    // 'angular/file-name': ['error', {
    //   nameStyle: 'dash',
    //   typeSeparator: 'dot',
    //   ignoreTypeSuffix: true
    // }], // require and specify a consistent component name pattern (y120, y121)
    // 'angular/filter-name': ['error', 'ds'], // require and specify a prefix for all filter names
    'angular/module-name': 'off', // require and specify a prefix for all module names (y127)
    'angular/service-name': 'off', // require and specify a prefix for all service names (y125)
    // Angular : Naming }}}

    // {{{ Angular : Conventions
    'angular/di-order': 'off', // require DI parameters to be sorted alphabetically
    // 'angular/di': [2, '$inject'], // require a consistent DI syntax
    'angular/dumb-inject': 'error', // unittest inject functions should only consist of assignments from injected values to describe block variables
    // 'angular/function-type': ['error', 'named'], // require and specify a consistent function style for components ('named' or 'anonymous') (y024)
    // 'angular/module-dependency-order': ['error', {
    //   grouped: true,
    //   prefix: 'app'
    // }], // require a consistent order of module dependencies
    'angular/no-service-method': 'off', // use factory() instead of service() (y040)
    'angular/one-dependency-per-line': 'off', // require all DI parameters to be located in their own line
    'angular/rest-service': ['error', '$http'], // disallow different rest service and specify one of '$http', '$resource', 'Restangular'
    'angular/watchers-execution': 'off', // require and specify consistent use $scope.digest() or $scope.apply()
    // Angular : Conventions}}}

    // {{{ Angular : Angular Wrappers
    // 'angular/angularelement': 'error', // use angular.element instead of $ or jQuery
    'angular/definedundefined': 'off', // use angular.isDefined and angular.isUndefined instead of other undefined checks
    // 'angular/document-service': 'error', // use $document instead of document (y180)
    'angular/foreach': 'off', // use angular.forEach instead of native Array.prototype.forEach
    'angular/interval-service': 'error', // use $interval instead of setInterval (y181)
    'angular/json-functions': 'off', // use angular.fromJson and 'angular.toJson' instead of JSON.parse and JSON.stringify
    // 'angular/log': 'error', // use the $log service instead of the console methods
    'angular/no-angular-mock': 'off', // require to use angular.mock methods directly
    'angular/no-jquery-angularelement': 'error', // disallow to wrap angular.element objects with jQuery or $
    'angular/timeout-service': 'error', // use $timeout instead of setTimeout (y181)
    'angular/typecheck-array': 'off', // use angular.isArray instead of typeof comparisons
    'angular/typecheck-date': 'off', // use angular.isDate instead of typeof comparisons
    'angular/typecheck-function': 'off', // use angular.isFunction instead of typeof comparisons
    'angular/typecheck-number': 'off', // use angular.isNumber instead of typeof comparisons
    'angular/typecheck-object': 'off', // use angular.isObject instead of typeof comparisons
    'angular/typecheck-string': 'off' // use angular.isString instead of typeof comparisons
    // 'angular/window-service': 'error' // use $window instead of window (y180)
    // Angular : Angular Wrappers }}}

    // Angular }}}
  }
};
