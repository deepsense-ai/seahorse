/* @ngInject */
function UnsupportedConfig($stateProvider, $urlRouterProvider) {
  'use strict';

  $stateProvider.state('home-unsupported', {
    url: '/',
    template: `
      <div class='alert alert-danger' role='alert' style='font-size: 18px;'>
        Sorry, this browser is currently unsupported.
        <br/>
        Please launch Chrome ver. 40 or newer.
      </div>
    `
  });

  $urlRouterProvider.otherwise('/');
}

/* @ngInject */
function UnsupportedRun($rootScope) {
  'use strict';

  $rootScope.stateData = {};
  $rootScope.stateData.dataIsLoaded = true;
  $rootScope.stateData.showView = true;
}

exports.inject = function(module) {
  'use strict';

  module.config(UnsupportedConfig);
  module.run(UnsupportedRun);
};
