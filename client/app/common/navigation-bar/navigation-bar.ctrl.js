'use strict';

/* @ngInject */
function NavigationController($rootScope, PageService) {
  _.assign(this, {
    getTitle() {
        return PageService.getTitle();
      },
      home() {
        $rootScope.$broadcast('StatusBar.HOME_CLICK');
      }
  });
}

exports.inject = function(module) {
  module.controller('NavigationController', NavigationController);
};
