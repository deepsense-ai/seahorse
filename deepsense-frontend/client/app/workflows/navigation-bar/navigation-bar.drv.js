'use strict';

/* @ngInject */
function NavigationBar() {
  return {
    templateUrl: 'app/workflows/navigation-bar/navigation-bar.html',
    controllerAs: 'controller',
    controller: 'NavigationController'
  };
}

exports.inject = function(module) {
  module.directive('navigationBar', NavigationBar);
};
