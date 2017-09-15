'use strict';

/* @ngInject */
function MenuItem() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/menu-item/menu-item.html',
    replace: true,
    controller: 'MenuItemController as miCtrl',
    scope: {
      label: '@',
      icon: '@',
      callFunction: '@'
    },
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('menuItem', MenuItem);
};
