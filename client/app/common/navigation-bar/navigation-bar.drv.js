'use strict';

/* @ngInject */
function NavigationBar() {
  return {
    scope: {
      title: '='
    },
    templateUrl: 'app/common/navigation-bar/navigation-bar.html'
  };
}
exports.function = NavigationBar;

exports.inject = function (module) {
  module.directive('navigationBar', NavigationBar);
};
