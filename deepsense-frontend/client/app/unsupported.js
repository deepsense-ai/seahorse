'use strict';
/* @ngInject */
function UnsupportedConfig($stateProvider, $urlRouterProvider) {
  $stateProvider.state('home-unsupported', {
    url: '/',
    template: `
      <div class='alert alert-danger' role='alert' style='font-size: 18px;'>
       We're sorry, Seahorse doesn't support your browser yet.<br/>We're working on it, please use Google Chrome in the meantime.
      </div>
    `
  });

  $urlRouterProvider.otherwise('/');
}

/* @ngInject */
function UnsupportedRun($rootScope) {
  $rootScope.stateData = {};
  $rootScope.stateData.dataIsLoaded = true;
  $rootScope.stateData.showView = true;
}

exports.inject = function(module) {
  module.config(UnsupportedConfig);
  module.run(UnsupportedRun);
};
