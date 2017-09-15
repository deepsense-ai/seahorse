'use strict';

/* @ngInject */
function NavigationBar() {
  return {
    templateUrl: 'app/workflows/navigation-bar/navigation-bar.html'
  };
}
exports.function = NavigationBar;

exports.inject = function(module) {
  module.directive('navigationBar', NavigationBar);
};
