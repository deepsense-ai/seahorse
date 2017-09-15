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
