'use strict';

/* @ngInject */
function CustomScrollBar() {
  return {
    restrict: 'A',
    link: (scope, element) => {
      jQuery(element)
        .mCustomScrollbar({
          axis: 'y',
          theme: 'deepsense',
          advanced: { autoScrollOnFocus: false },
          scrollInertia: 300
        });
    }
  };
}

exports.inject = function(module) {
  module.directive('customScrollBar', CustomScrollBar);
};
