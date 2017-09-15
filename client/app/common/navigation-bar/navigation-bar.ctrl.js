'use strict';

/* @ngInject */
function NavigationController($rootScope, config, PageService) {
  _.assign(this, {
      getTitle() {
        return PageService.getTitle();
      },
      home() {
        $rootScope.$broadcast('StatusBar.HOME_CLICK');
      },
      getAPIVersion() {
        return config.apiVersion;
      }
  });
}

exports.inject = function(module) {
  module.controller('NavigationController', NavigationController);
};
