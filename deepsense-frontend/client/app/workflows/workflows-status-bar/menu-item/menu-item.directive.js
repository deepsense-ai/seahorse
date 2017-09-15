'use strict';

/* @ngInject */
function MenuItem() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/menu-item/menu-item.html',
    replace: true,
    scope: {
      label: '@',
      icon: '@',
      callFunction: '&',
      href: '@',
      target: '@',
      color: '@',
      additionalClass: '@'
    },
    controller: 'MenuItemController as miCtrl',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('menuItem', MenuItem);
};
