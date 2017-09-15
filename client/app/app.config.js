/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function AppConfig($stateProvider, $urlRouterProvider, toastrConfig) {
  // TODO: enable html5mode

  angular.extend(toastrConfig, {
    'allowHtml': true,
    'newestOnTop': false,
    'positionClass': 'toast-bottom-left',
    'progressBar': true,
    'timeOut': 3500,
    'iconClasses': {
      'error': 'notification--error fa-exclamation-circle',
      'info': 'toast-info',
      'success': 'toast-success',
      'warning': 'toast-warning'
    }
  });

  $urlRouterProvider.otherwise('/');

  $stateProvider.state('lab', {
    abstract: true,
    template: '<div ui-view/>'
  });
}

exports.function = AppConfig;

exports.inject = function (module) {
  module.config(AppConfig);
};
