'use strict';

import tpl from './sticky-window.html';

function StickyWindow() {
  return {
    restrict: 'E',
    scope: {
      'x': '=',
      'y': '='
    },
    transclude: true,
    templateUrl: tpl
  };
}

exports.inject = function(module) {
  module.directive('stickyWindow', StickyWindow);
};
