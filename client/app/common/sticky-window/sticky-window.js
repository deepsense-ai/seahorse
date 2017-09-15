/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function StickyWindow() {
  return {
    restrict: 'E',
    scope: {
      'x': '=',
      'y': '='
    },
    transclude: true,
    templateUrl: 'app/common/sticky-window/sticky-window.html'
  };
}

exports.inject = function (module) {
  module.directive('stickyWindow', StickyWindow);
};
