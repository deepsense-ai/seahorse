'use strict';

import tpl from './navigation-bar.html';

/* @ngInject */
function NavigationBar() {
  return {
    templateUrl: tpl,
    controllerAs: 'controller',
    controller: 'NavigationController'
  };
}

exports.inject = function(module) {
  module.directive('navigationBar', NavigationBar);
};
