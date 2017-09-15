/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 01.07.15.
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

angular.module('deepsense.attributes-panel')
  .directive('customScrollBar', CustomScrollBar);