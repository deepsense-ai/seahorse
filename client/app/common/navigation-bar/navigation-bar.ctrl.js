'use strict';

/* @ngInject */
function NavigationController($rootScope, PageService) {
  var that = this;

  that.getTitle = function getTitle() {
    return PageService.getTitle();
  };

  return this;
}

exports.inject = function (module) {
  module.controller('NavigationController', NavigationController);
};
