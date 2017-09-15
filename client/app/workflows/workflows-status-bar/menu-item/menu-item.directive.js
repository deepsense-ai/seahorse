'use strict';

import tpl from './menu-item.html';

/* @ngInject */
function MenuItem() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      label: '@',
      forOwnerOnly: '@',
      icon: '@',
      callFunction: '&',
      href: '@',
      target: '@',
      color: '@',
      additionalClass: '@',
      additionalIconClass: '@',
      additionalHtmlForOwner: '@'
    },
    controller: 'MenuItemController as miCtrl',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('menuItem', MenuItem);
};
