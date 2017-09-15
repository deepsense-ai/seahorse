'use strict';

/* @ngInject */
function NavigationController($rootScope, PageService) {
  var that = this;

  that.getTitle = function getTitle() {
    return PageService.getTitle();
  };

  that.home = function home () {
    $rootScope.$broadcast('StatusBar.HOME_CLICK');
  };

  return this;
}

exports.inject = function (module) {
  module.controller('NavigationController', NavigationController);
};
