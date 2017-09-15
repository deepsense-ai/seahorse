/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 26.06.15.
 */

'use strict';

/* @ngInject */
function CustomScrollBar() {
  return {
    restrict: 'A',
    link: (scope, element) => {
      jQuery(element).mCustomScrollbar({
        axis: 'y',
        theme: 'deepsense',
        scrollInertia: 300
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('customScrollBar', CustomScrollBar);
};
