'use strict';

/* @ngInject */
function Toggle() {
  return {
    restrict: 'A',
    link: (scope, element, attrs) => {

      let classesOn = `${attrs.toggleAnimateIn}`;
      let classesOff = `${attrs.toggleAnimateOut}`;

      attrs.toggleOn.split(' ').forEach((eventName) => {
        scope.$on(eventName, () => {
          element
            .removeClass(classesOn)
            .addClass(classesOff)
            .one('animationend', function() {
              $(this).removeClass(classesOn);
            });
        });
      });

      scope.$on(attrs.toggleOff, () => {
        element
          .removeClass(classesOff)
          .addClass(classesOn);
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('toggle', Toggle);
};
