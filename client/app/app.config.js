'use strict';

/* @ngInject */
function AppConfig($urlRouterProvider, toastrConfig) {
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
}

exports.function = AppConfig;

exports.inject = function (module) {
  module.config(AppConfig);
};
